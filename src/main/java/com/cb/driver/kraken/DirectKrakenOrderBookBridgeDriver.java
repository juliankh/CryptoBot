package com.cb.driver.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.JsonSerializer;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbReadOnlyProvider;
import com.cb.db.MiscConfigName;
import com.cb.driver.AbstractDriver;
import com.cb.driver.kraken.args.KrakenOrderBookBridgeArgsConverter;
import com.cb.injection.module.MainModule;
import com.cb.model.config.KrakenBridgeOrderBookConfig;
import com.cb.model.kraken.ws.request.KrakenOrderBookSubscriptionRequest;
import com.cb.model.kraken.ws.request.KrakenOrderBookSubscriptionRequestParams;
import com.cb.processor.checksum.ChecksumCalculator;
import com.cb.processor.kraken.KrakenJsonOrderBookProcessor;
import com.cb.ws.WebSocketClient;
import com.google.common.collect.Lists;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.CurrencyPair;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;

import static com.cb.injection.BindingName.*;

@Slf4j
public class DirectKrakenOrderBookBridgeDriver extends AbstractDriver {

    private static final int SLEEP_SECS_CONNECTIVITY_CHECK = 2;

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    @Named(KRAKEN_CHECKSUM_CALCULATOR)
    private ChecksumCalculator checksumCalculator;

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
        DirectKrakenOrderBookBridgeDriver driver = MainModule.INJECTOR.getInstance(DirectKrakenOrderBookBridgeDriver.class);
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
        KrakenBridgeOrderBookConfig config = configMap.get(currencyPair);
        log.info("Config: " + config);
        int batchSize = config.getBatchSize();
        maxSecsBetweenUpdates = config.getSecsTimeout();
        int depth = dbReadOnlyProvider.miscConfig(MiscConfigName.KRAKEN_ORDER_BOOK_DEPTH).intValue();
        dbReadOnlyProvider.cleanup();
        this.depth = depth;
        ((KrakenJsonOrderBookProcessor)webSocketClient.getJsonProcessor()).initialize(webSocketClient.getRequestId(), currencyPair, depth, batchSize, checksumCalculator);
    }

    @Override
    protected void executeCustom() {
        log.info("Max Secs Between Updates: " + maxSecsBetweenUpdates);
        WebSocket webSocket = connect();
        while (true) {
            boolean webSocketClosed = webSocketClosed(webSocket);
            boolean latestReceiveAgeOverLimit = latestReceiveAgeOverLimit(webSocketClient.getLatestReceive().get(), Instant.now(), maxSecsBetweenUpdates);
            if (webSocketClosed || latestReceiveAgeOverLimit) {
                // TODO: manually verify
                String msg = "Will try to reconnect to WebSocket because " + (webSocketClosed ? "WebSocket is closed" : "no data received in over " + maxSecsBetweenUpdates + " secs");
                log.warn(msg);
                alertProvider.sendEmailAlertQuietly("Reconn - " + getDriverName(), msg);
                if (latestReceiveAgeOverLimit) {
                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, msg).join();
                }
                webSocket = connect();
            }
            TimeUtils.sleepQuietlyForSecs(SLEEP_SECS_CONNECTIVITY_CHECK);
        }
    }

    public boolean webSocketClosed(WebSocket webSocket) {
        boolean inputClosed = webSocket.isInputClosed();
        boolean outputClosed = webSocket.isOutputClosed();
        if (inputClosed || outputClosed) {
            log.warn("Input Closed? [" + inputClosed + "]; Output Closed? [" + outputClosed + "]");
            return true;
        }
        return false;
    }

    public boolean latestReceiveAgeOverLimit(Instant latestReceive, Instant timeToCompareTo, int maxSecsBetweenUpdates) {
        if (latestReceive == null) {
            return false;
        }
        long secsSinceLastUpdate = ChronoUnit.SECONDS.between(latestReceive, timeToCompareTo);
        if (secsSinceLastUpdate > maxSecsBetweenUpdates) {
            log.warn("It's been [" + secsSinceLastUpdate + "] secs since data was last received, which is above the threshold of [" + maxSecsBetweenUpdates + "] secs");
            return true;
        }
        return false;
    }

    @SneakyThrows
    private WebSocket connect() {
        KrakenOrderBookSubscriptionRequest subscription = new KrakenOrderBookSubscriptionRequest()
                .setReq_id(webSocketClient.getRequestId())
                .setParams(new KrakenOrderBookSubscriptionRequestParams()
                        .setSnapshot(true)
                        .setDepth(depth)
                        .setSymbol(Lists.newArrayList(currencyPair.toString())));
        String subscriptionString = jsonSerializer.serializeToJson(subscription);
        log.info("WebSocket Subscription: [" + subscriptionString + "]");
        WebSocket webSocket = HttpClient.newHttpClient().newWebSocketBuilder().buildAsync(URI.create(webSocketUrl), webSocketClient).join();
        webSocket.sendText(subscriptionString, true);
        return webSocket;
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        webSocketClient.cleanup();
        dbReadOnlyProvider.cleanup();
    }

}
