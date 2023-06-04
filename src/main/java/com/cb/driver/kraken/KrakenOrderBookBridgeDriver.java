package com.cb.driver.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbReadOnlyProvider;
import com.cb.db.MiscConfigName;
import com.cb.driver.AbstractDriver;
import com.cb.driver.kraken.args.KrakenOrderBookBridgeArgsConverter;
import com.cb.injection.module.MainModule;
import com.cb.model.config.KrakenBridgeOrderBookConfig;
import com.cb.processor.kraken.KrakenJsonOrderBookProcessor;
import com.cb.ws.WebSocketClient;
import com.cb.ws.kraken.request.OrderBookSubscription;
import com.cb.ws.kraken.request.OrderBookSubscriptionParams;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.CurrencyPair;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.cb.injection.BindingName.KRAKEN_WEBSOCKET_V2_CLIENT_ORDER_BOOK;
import static com.cb.injection.BindingName.KRAKEN_WEBSOCKET_V2_URL;

@Slf4j
public class KrakenOrderBookBridgeDriver extends AbstractDriver {

    private static final int SLEEP_SECS_CONNECTIVITY_CHECK = 2;

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    @Named(KRAKEN_WEBSOCKET_V2_URL)
    private String webSocketUrl;

    @Inject
    @Named(KRAKEN_WEBSOCKET_V2_CLIENT_ORDER_BOOK)
    private WebSocketClient webSocketClient;

    private CurrencyPair currencyPair;
    private int depth;
    private String driverName;
    private int maxSecsBetweenUpdates;

    public static void main(String[] args) {
        KrakenOrderBookBridgeDriver driver = MainModule.INJECTOR.getInstance(KrakenOrderBookBridgeDriver.class);
        driver.initialize(args);
        driver.execute();
    }

    public void initialize(String[] args) {
        KrakenOrderBookBridgeArgsConverter argsConverter = new KrakenOrderBookBridgeArgsConverter(args);
        currencyPair = argsConverter.getCurrencyPair();
        String currencyToken = currencyResolver.upperCaseToken(currencyPair, "-");
        String driverToken = argsConverter.getDriverToken();
        driverName = "D Kr OB Bridge (" + currencyToken + ")" + (StringUtils.isBlank(driverToken) ? "" : " " + driverToken);
        Map<CurrencyPair, KrakenBridgeOrderBookConfig> configMap = dbReadOnlyProvider.krakenBridgeOrderBookConfig();
        dbReadOnlyProvider.cleanup();
        KrakenBridgeOrderBookConfig config = configMap.get(currencyPair);
        log.info("Config: " + config);
        int batchSize = config.getBatchSize();
        maxSecsBetweenUpdates = config.getSecsTimeout();
        int depth = dbReadOnlyProvider.miscConfig(MiscConfigName.KRAKEN_ORDER_BOOK_DEPTH).intValue(); // TODO: manually test
        this.depth = depth;
        ((KrakenJsonOrderBookProcessor)webSocketClient.getJsonProcessor()).initialize(currencyPair, depth, batchSize);
    }

    @Override
    protected void executeCustom() {
        log.info("Max Secs Between Updates: " + maxSecsBetweenUpdates);
        WebSocket webSocket = connect();
        while (true) {
            long secsSinceLastUpdate = ChronoUnit.SECONDS.between(webSocketClient.getLatestReceive().get(), Instant.now());
            if (secsSinceLastUpdate > maxSecsBetweenUpdates) {
                String msg = "It's been [" + secsSinceLastUpdate + "] secs since data was last received, which is above the threshold of [" + maxSecsBetweenUpdates + "] secs, so will try to reconnect";
                log.warn(msg);
                alertProvider.sendEmailAlertQuietly("Reconn - " + getDriverName(), msg);
                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, msg).join();
                webSocket = connect();
            }
            TimeUtils.sleepQuietlyForSecs(SLEEP_SECS_CONNECTIVITY_CHECK);
        }
    }

    @SneakyThrows
    private WebSocket connect() {
        OrderBookSubscription subscription = new OrderBookSubscription()
                .setReq_id(2746) // TODO: create a new one and save locally, and verify for this value when receiving data
                .setParams(new OrderBookSubscriptionParams()
                        .setSnapshot(true)
                        .setDepth(depth)
                        .setSymbol(Lists.newArrayList(currencyPair.toString())));
        String subscriptionString = ObjectConverter.OBJECT_MAPPER.writer().writeValueAsString(subscription);
        log.info("WebSocket Subscription: [" + subscriptionString + "]");
        WebSocket webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(webSocketUrl), webSocketClient).join();
        CompletableFuture.runAsync(() -> {
            webSocket.sendText(subscriptionString, true);
            TimeUtils.awaitQuietly(webSocketClient.getLatch());
        });
        return webSocket;
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        dbReadOnlyProvider.cleanup();
    }

}
