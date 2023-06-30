package com.cb.driver.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.JsonSerializer;
import com.cb.common.SleepDelegate;
import com.cb.common.util.GeneralUtils;
import com.cb.db.MiscConfigName;
import com.cb.db.ReadOnlyDao;
import com.cb.driver.AbstractDriver;
import com.cb.driver.kraken.args.KrakenOrderBookBridgeArgsConverter;
import com.cb.injection.module.MainModule;
import com.cb.injection.provider.KrakenOrderBookWebSocketClientProvider;
import com.cb.model.CbOrderBook;
import com.cb.model.config.KrakenBridgeOrderBookConfig;
import com.cb.model.kraken.ws.request.KrakenOrderBookSubscriptionRequest;
import com.cb.model.kraken.ws.request.KrakenOrderBookSubscriptionRequestParams;
import com.cb.processor.checksum.ChecksumCalculator;
import com.cb.processor.kraken.KrakenOrderBookDelegate;
import com.cb.processor.kraken.channel_status.KrakenChannelStatus;
import com.cb.processor.kraken.json.KrakenJsonOrderBookProcessor;
import com.cb.ws.WebSocketClient;
import com.cb.ws.WebSocketFactory;
import com.cb.ws.WebSocketStatusCode;
import com.google.common.collect.Lists;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.CurrencyPair;

import javax.inject.Inject;
import javax.inject.Named;
import java.net.http.WebSocket;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

import static com.cb.injection.BindingName.KRAKEN_CHECKSUM_CALCULATOR;
import static com.cb.injection.BindingName.KRAKEN_WEBSOCKET_V2_URL;

@Slf4j
@Setter
public class DirectKrakenOrderBookBridgeDriver extends AbstractDriver {

    public static final int SLEEP_SECS_CONNECTIVITY_CHECK = 8;
    public static final int THROTTLE_SLEEP_SECS = 15;

    @Inject
    private ReadOnlyDao readOnlyDao;

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    @Named(KRAKEN_CHECKSUM_CALCULATOR)
    private ChecksumCalculator checksumCalculator;

    @Inject
    private WebSocketFactory webSocketFactory;

    @Inject
    @Named(KRAKEN_WEBSOCKET_V2_URL)
    private String webSocketUrl;

    @Inject
    private KrakenOrderBookWebSocketClientProvider krakenOrderBookWebSocketClientProvider;

    @Inject
    private SleepDelegate sleepDelegate;

    @Inject
    private KrakenOrderBookDelegate orderBookDelegate;

    private WebSocket webSocket;
    private WebSocketClient webSocketClient;
    private CurrencyPair currencyPair;
    private int depth;
    private int batchSize;
    private String driverName;
    private int maxSecsBetweenUpdates;

    public static void main(String[] args) {
        DirectKrakenOrderBookBridgeDriver driver = MainModule.INJECTOR.getInstance(DirectKrakenOrderBookBridgeDriver.class);
        driver.initialize(args);
        driver.execute();
    }

    public void initialize(String[] args) {
        log.info("Initializing Driver");
        KrakenOrderBookBridgeArgsConverter argsConverter = new KrakenOrderBookBridgeArgsConverter(args);
        currencyPair = argsConverter.getCurrencyPair();
        String currencyToken = currencyResolver.upperCaseToken(currencyPair, "-");
        String driverToken = argsConverter.getDriverToken();
        driverName = "D Kr OB Bridge (" + currencyToken + ")" + (StringUtils.isBlank(driverToken) ? "" : " " + driverToken);
        Map<CurrencyPair, KrakenBridgeOrderBookConfig> configMap = readOnlyDao.krakenBridgeOrderBookConfig();
        KrakenBridgeOrderBookConfig config = configMap.get(currencyPair);
        log.info("Config: " + config);
        batchSize = config.getBatchSize();
        maxSecsBetweenUpdates = config.getSecsTimeout();
        int depth = readOnlyDao.miscConfig(MiscConfigName.KRAKEN_ORDER_BOOK_DEPTH).intValue();
        readOnlyDao.cleanup();
        this.depth = depth;
        initializeWebSocketClient();
    }

    public void initializeWebSocketClient() {
        log.info("Initializing WebSocketClient");
        webSocketClient = krakenOrderBookWebSocketClientProvider.get();
        ((KrakenJsonOrderBookProcessor)webSocketClient.getJsonProcessor()).initialize(getDriverName(), webSocketClient.getRequestId(), currencyPair, depth, batchSize, checksumCalculator);
    }

    @Override
    protected void executeCustom() {
        log.info("Max Secs Between Updates: " + maxSecsBetweenUpdates);
        webSocket = connect();
        while (true) {
            executeIteration(Instant.now());
            sleepDelegate.sleepQuietlyForSecs(SLEEP_SECS_CONNECTIVITY_CHECK);
        }
    }

    public void executeIteration(Instant timeToCompareTo) {
        boolean webSocketClosed = webSocketClosed(webSocket);
        boolean orderBookStale = orderBookDelegate.orderBookStale(latestOrderBookExchangeDateTimeSupplier(), channelStatusSupplier(), maxSecsBetweenUpdates, timeToCompareTo);
        if (webSocketClosed || orderBookStale) {
            Integer statusCode = webSocketClient.getCloseStatusCode();
            if (statusCode != null && statusCode == WebSocketStatusCode.TRY_AGAIN_LATER) {
                log.info("Got WebSocket Close StatusCode [" + statusCode + "] which means that requests are being throttled.  Therefore will sleep for [" + THROTTLE_SLEEP_SECS + "] secs before trying to reconnect.");
                sleepDelegate.sleepQuietlyForSecs(THROTTLE_SLEEP_SECS);
            }
            String reason = webSocketClient.getCloseReason();
            String msg = "Will try to reconnect to WebSocket because " + (webSocketClosed ? "WebSocket is closed (Status Code [" + statusCode + "], Reason [" + reason + "])" : "latest OrderBook Snapshot is older then " + maxSecsBetweenUpdates + " secs");
            log.warn(msg);
            alerter.sendEmailAlertQuietly("Reconn - " + getDriverName(), msg);
            if (orderBookStale) {
                log.info("Will try to close WebSocket");
                GeneralUtils.runQuietly(() -> webSocket.sendClose(WebSocket.NORMAL_CLOSURE, msg).join()); // TODO: what's the difference between close or unsubscribe msg?
                log.info("Request to close WebSocket sent");
            }
            initializeWebSocketClient();
            webSocket = connect();
        }
    }

    public Supplier<Instant> latestOrderBookExchangeDateTimeSupplier() {
        return () -> Optional.ofNullable(latestOrderBook()).map(CbOrderBook::getExchangeDatetime).orElse(null);
    }

    private CbOrderBook latestOrderBook() {
        return ((KrakenJsonOrderBookProcessor)webSocketClient.getJsonProcessor()).latestOrderBookSnapshot();
    }

    public Supplier<KrakenChannelStatus> channelStatusSupplier() {
        return () -> ((KrakenJsonOrderBookProcessor)webSocketClient.getJsonProcessor()).currentChannelStatus();
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
        WebSocket webSocket = webSocketFactory.webSocket(webSocketUrl, webSocketClient);
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
        readOnlyDao.cleanup();
    }

}
