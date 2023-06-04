package com.cb.driver.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbReadOnlyProvider;
import com.cb.driver.AbstractDriver;
import com.cb.driver.kraken.args.KrakenOrderBookBridgeArgsConverter;
import com.cb.injection.module.MainModule;
import com.cb.model.config.KrakenBridgeOrderBookConfig;
import com.cb.processor.kraken.XchangeKrakenOrderBookBridgeProcessor;
import com.google.inject.Inject;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakeException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class XchangeKrakenOrderBookBridgeDriver extends AbstractDriver {

    private static final int SLEEP_SECS_CONNECTIVITY_CHECK = 2;
    private static final int ORDER_BOOK_DEPTH = 500;

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private XchangeKrakenOrderBookBridgeProcessor processor;

    private CurrencyPair currencyPair;
    private String driverName;
    private int maxSecsBetweenUpdates;

    private final AtomicReference<Instant> latestReceive = new AtomicReference<>();

    public static void main(String[] args) {
        try {
            XchangeKrakenOrderBookBridgeDriver driver = MainModule.INJECTOR.getInstance(XchangeKrakenOrderBookBridgeDriver.class);
            driver.initialize(args);
            driver.execute();
        } catch (WebSocketClientHandshakeException e) {
            if (e.getMessage().contains("Too Many Requests")) {
                log.error("TOO MANY REQUESTS -- so need to exit the process to prevent further requests (which will fail anyway) from being made");
                System.exit(1);
            }
        }
    }

    public void initialize(String[] args) {
        KrakenOrderBookBridgeArgsConverter argsConverter = new KrakenOrderBookBridgeArgsConverter(args);
        currencyPair = argsConverter.getCurrencyPair();
        String currencyToken = currencyResolver.upperCaseToken(currencyPair, "-");
        String driverToken = argsConverter.getDriverToken();
        driverName = "Xch Kr OB Bridge (" + currencyToken + ")" + (StringUtils.isBlank(driverToken) ? "" : " " + driverToken);
        Map<CurrencyPair, KrakenBridgeOrderBookConfig> configMap = dbReadOnlyProvider.krakenBridgeOrderBookConfig();
        dbReadOnlyProvider.cleanup();
        KrakenBridgeOrderBookConfig config = configMap.get(currencyPair);
        log.info("Config: " + config);
        int batchSize = config.getBatchSize();
        maxSecsBetweenUpdates = config.getSecsTimeout();
        processor.initialize(batchSize);
    }

    @Override
    protected void executeCustom() {
        log.info("Max Secs Between Updates: " + maxSecsBetweenUpdates);
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
        StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        subscribe(krakenExchange);
        latestReceive.set(Instant.now());
        while (true) {
            long secsSinceLastUpdate = ChronoUnit.SECONDS.between(latestReceive.get(), Instant.now());
            if (secsSinceLastUpdate > maxSecsBetweenUpdates) {
                String msg = "It's been [" + secsSinceLastUpdate + "] secs since data was last received, which is above the threshold of [" + maxSecsBetweenUpdates + "] secs, so will try to reconnect";
                log.warn(msg);
                alertProvider.sendEmailAlertQuietly("Reconn - " + getDriverName(), msg);
                krakenExchange.disconnect().blockingAwait();
                subscribe(krakenExchange);
            }
            TimeUtils.sleepQuietlyForSecs(SLEEP_SECS_CONNECTIVITY_CHECK);
        }
    }

    private void subscribe(StreamingExchange krakenExchange) {
        log.info("Subscribing for [" + currencyPair + "]");
        krakenExchange.connect().blockingAwait();
        krakenExchange
                .getStreamingMarketDataService()
                .getOrderBook(currencyPair, ORDER_BOOK_DEPTH)
                .onTerminateDetach()
                .subscribe(
                        orderBook -> {
                            latestReceive.set(Instant.now());
                            processor.process(orderBook, currencyPair, getDriverName());
                        },
                        throwable -> {
                            String msg = "Error in Process [" + getDriverName() + "] while listening to OrderBooks: " + throwable.getMessage();
                            log.error(msg, throwable);
                            alertProvider.sendEmailAlertQuietly(msg, msg, throwable);
                        }
                );
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        dbReadOnlyProvider.cleanup();
        processor.cleanup();
    }

}
