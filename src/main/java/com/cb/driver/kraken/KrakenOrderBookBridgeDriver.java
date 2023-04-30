package com.cb.driver.kraken;

import com.cb.alert.AlertProvider;
import com.cb.common.CurrencyResolver;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbProvider;
import com.cb.driver.AbstractDriver;
import com.cb.driver.kraken.args.KrakenOrderBookBridgeArgsConverter;
import com.cb.jms.common.JmsPublisher;
import com.cb.model.config.KrakenBridgeOrderBookConfig;
import com.cb.model.kraken.jms.KrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.cb.processor.BatchProcessor;
import com.cb.processor.kraken.KrakenOrderBookBridgeProcessor;
import com.cb.property.CryptoProperties;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.reactivex.disposables.Disposable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KrakenOrderBookBridgeDriver extends AbstractDriver {

    private static final int SLEEP_SECS_CONNECTIVITY_CHECK = 2;
    private static final int SLEEP_SECS_RECONNECT = 15;
    private static final int ORDER_BOOK_DEPTH = 500; // TODO: confirm that all currency pairs have this much depth

    private final KrakenOrderBookBridgeProcessor processor;
    private final CurrencyPair currencyPair;
    private final int maxSecsBetweenUpdates;
    private final AtomicReference<Instant> latestReceive = new AtomicReference<>();
    private final String driverName;

    private Throwable throwable;

    public static void main(String[] args) {
        KrakenOrderBookBridgeArgsConverter argsConverter = new KrakenOrderBookBridgeArgsConverter(args);
        String driverToken = argsConverter.getDriverToken();
        CurrencyPair currencyPair = argsConverter.getCurrencyPair();
        KrakenBridgeOrderBookConfig config = currencyPairConfig(currencyPair);
        log.info("Config: " + config);
        int batchSize = config.getBatchSize();
        int maxSecsBetweenUpdates = config.getSecsTimeout();
        BatchProcessor<KrakenOrderBook, KrakenOrderBookBatch> batchProcessor = new BatchProcessor<>(batchSize);
        CryptoProperties properties = new CryptoProperties();
        String jmsDestination = properties.jmsKrakenOrderBookSnapshotQueueName();
        String jmsExchange = properties.jmsKrakenOrderBookSnapshotQueueExchange();
        JmsPublisher<KrakenOrderBookBatch> jmsPublisher = new JmsPublisher<>(jmsDestination, jmsExchange);
        KrakenOrderBookBridgeProcessor processor = new KrakenOrderBookBridgeProcessor(batchProcessor, jmsPublisher);
        AlertProvider alertProvider = new AlertProvider();
        (new KrakenOrderBookBridgeDriver(driverToken, currencyPair, processor, maxSecsBetweenUpdates, alertProvider)).execute();
    }

    @SneakyThrows
    public KrakenOrderBookBridgeDriver(String driverNameToken, CurrencyPair currencyPair, KrakenOrderBookBridgeProcessor processor, int maxSecsBetweenUpdates, AlertProvider alertProvider) {
        super(alertProvider);
        this.currencyPair = currencyPair;
        this.processor = processor;
        this.maxSecsBetweenUpdates = maxSecsBetweenUpdates;

        CurrencyResolver tokenResolver = new CurrencyResolver();
        String currencyToken = tokenResolver.upperCaseToken(currencyPair, "-");

        this.driverName = "Kraken OrderBook Bridge (" + currencyToken + ")" + (StringUtils.isBlank(driverNameToken) ? "" : " " + driverNameToken);
    }

    private static KrakenBridgeOrderBookConfig currencyPairConfig(CurrencyPair currencyPair) {
        DbProvider dbProvider = new DbProvider();
        Map<CurrencyPair, KrakenBridgeOrderBookConfig> configMap = dbProvider.retrieveKrakenBridgeOrderBookConfig();
        return configMap.get(currencyPair);
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    protected void executeCustom() {
        log.info("Max Secs Between Updates: " + maxSecsBetweenUpdates);
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
        StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        Disposable disposable = subscribe(krakenExchange, currencyPair);
        maintainConnectivity(krakenExchange, disposable);
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        processor.cleanup();
    }

    private Disposable subscribe(StreamingExchange krakenExchange, CurrencyPair currencyPair) {
        log.info("Subscribing for [" + currencyPair + "]");
        krakenExchange.connect().blockingAwait();
        latestReceive.set(Instant.now());
        return krakenExchange
                .getStreamingMarketDataService()
                .getOrderBook(currencyPair, ORDER_BOOK_DEPTH)
                .onTerminateDetach()
                .doOnEach(orderBookNotification -> {
                    latestReceive.set(Instant.now());
                })
                .subscribe(
                        orderBook -> processor.process(orderBook, currencyPair, getDriverName()),
                        throwable -> {
                            log.error("Failed to get OrderBook: {}", throwable.getMessage(), throwable);
                            this.throwable = throwable;
                            krakenExchange.disconnect().subscribe(() -> log.info("Exchange Disconnected!"));
                        }
                );
    }

    private void maintainConnectivity(StreamingExchange krakenExchange, Disposable disposable) {
        while (true) {
            long secsSinceLastUpdate = ChronoUnit.SECONDS.between(latestReceive.get(), Instant.now());
            if (secsSinceLastUpdate > maxSecsBetweenUpdates) {
                String msg = "It's been [" + secsSinceLastUpdate + "] secs since data was last received, which is above the threshold of [" + maxSecsBetweenUpdates + "] secs, so will try to reconnect";
                log.warn(msg);
                alertProvider.sendEmailAlertQuietly(getDriverName() + " reconnecting", msg);
                disconnect(krakenExchange);
                TimeUtils.sleepQuietlyForSecs(SLEEP_SECS_RECONNECT);
                subscribe(krakenExchange, currencyPair);
            }
            if (disposable.isDisposed()) {
                disconnect(krakenExchange);
                throw new RuntimeException("Process [" + getDriverName() + "] unexpectedly stopped", throwable);
            }
            TimeUtils.sleepQuietlyForSecs(SLEEP_SECS_CONNECTIVITY_CHECK);
        }
    }

    private static void disconnect(StreamingExchange krakenExchange) {
        krakenExchange.disconnect().subscribe(() -> log.info("Exchange Disconnected!"));
    }

}
