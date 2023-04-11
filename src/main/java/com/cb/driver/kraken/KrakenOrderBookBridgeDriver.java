package com.cb.driver.kraken;

import com.cb.alert.AlertProvider;
import com.cb.driver.AbstractDriver;
import com.cb.driver.kraken.args.KrakenOrderBookBridgeArgsConverter;
import com.cb.processor.kraken.KrakenOrderBookBridgeProcessor;
import com.cb.util.CurrencyResolver;
import com.cb.util.TimeUtils;
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
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KrakenOrderBookBridgeDriver extends AbstractDriver {

    private static final int MAX_SECS_BETWEEN_UPDATES = 5;
    private static final int SLEEP_SECS_CONNECTIVITY_CHECK = 2;
    private static final int SLEEP_SECS_RECONNECT = 15;
    private static final int ORDER_BOOK_DEPTH = 500;
    private static final int BATCH_SIZE = 300;

    private final KrakenOrderBookBridgeProcessor processor;
    private final CurrencyPair currencyPair;
    private final AtomicReference<Instant> latestReceive = new AtomicReference<>();
    private final String driverName;

    private Throwable throwable;

    public static void main(String[] args) {
        KrakenOrderBookBridgeArgsConverter argsConverter = new KrakenOrderBookBridgeArgsConverter(args);
        AlertProvider alertProvider = new AlertProvider();
        KrakenOrderBookBridgeProcessor processor = new KrakenOrderBookBridgeProcessor(BATCH_SIZE, argsConverter.getCurrencyPair());
        (new KrakenOrderBookBridgeDriver(alertProvider, argsConverter.getDriverToken(), argsConverter.getCurrencyPair(), processor)).execute();
    }

    @SneakyThrows
    public KrakenOrderBookBridgeDriver(AlertProvider alertProvider, String driverNameToken, CurrencyPair currencyPair, KrakenOrderBookBridgeProcessor processor) {
        super(alertProvider);
        this.currencyPair = currencyPair;
        this.processor = processor;

        CurrencyResolver tokenResolver = new CurrencyResolver();
        String currencyToken = tokenResolver.upperCaseToken(currencyPair, "-");

        this.driverName = "Kraken OrderBook Bridge (" + currencyToken + ")" + (StringUtils.isBlank(driverNameToken) ? "" : " " + driverNameToken);
    }

    @Override
    public String getDriverName() {
        return driverName;
    }

    @Override
    protected void executeCustom() {
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
        StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        Disposable disposable = subscribe(krakenExchange, currencyPair);
        maintainConnectivity(krakenExchange, disposable);
    }

    @Override
    protected void cleanup() {
        processor.cleanup();
    }

    private void maintainConnectivity(StreamingExchange krakenExchange, Disposable disposable) {
        while (true) {
            long secsSinceLastUpdate = ChronoUnit.SECONDS.between(latestReceive.get(), Instant.now());
            if (secsSinceLastUpdate > MAX_SECS_BETWEEN_UPDATES) {
                String msg = "It's been [" + secsSinceLastUpdate + "] secs since data was last received, which is above the threshold of [" + MAX_SECS_BETWEEN_UPDATES + "] secs, so will try to reconnect";
                log.warn(msg);
                alertProvider.sendEmailAlert(getDriverName() + " reconnecting", msg);
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

    private static void disconnect(StreamingExchange krakenExchange) {
        krakenExchange.disconnect().subscribe(() -> log.info("Exchange Disconnected!"));
    }

}
