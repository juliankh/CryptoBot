package com.cb.driver.kraken;

import com.cb.alert.AlertProvider;
import com.cb.driver.AbstractDriver;
import com.cb.processor.kraken.KrakenOrderBookPersisterProcessor;
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

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KrakenOrderbookPersisterDriver extends AbstractDriver {

    private static final int MAX_SECS_BETWEEN_UPDATES = 55; // TODO: put back to 5
    private static final int SLEEP_SECS_CONNECTIVITY_CHECK = 2;
    private static final int SLEEP_SECS_RECONNECT = 15;
    private static final int ORDER_BOOK_DEPTH = 10; // TODO: put back to 500
    private static final int BATCH_SIZE = 20; // TODO: put back to 300

    private final KrakenOrderBookPersisterProcessor processor;
    private final CurrencyPair currencyPair;
    private final AtomicReference<Instant> latestReceive = new AtomicReference<>();
    private final String driverName;

    private Throwable throwable;

    @SneakyThrows
    public KrakenOrderbookPersisterDriver(AlertProvider alertProvider, String driverNamePostfix, CurrencyPair currencyPair, KrakenOrderBookPersisterProcessor processor) {
        super(alertProvider);
        this.currencyPair = currencyPair;
        this.processor = processor;
        this.driverName = "Kraken OrderBook Persister" + (StringUtils.isBlank(driverNamePostfix) ? "" : " " + driverNamePostfix);
    }

    public static void main(String[] args) throws IOException {
        String postfix = args.length == 0 || StringUtils.isBlank(args[0]) ? "" : args[0];
        AlertProvider alertProvider = new AlertProvider();
        //CurrencyPair currencyPair = CurrencyPair.BTC_USD; // TODO: put back
        CurrencyPair currencyPair = CurrencyPair.ATOM_USD;
        //CurrencyPair currencyPair = new CurrencyPair(Currency.ADA, Currency.AUD); // TODO: remove
        KrakenOrderBookPersisterProcessor processor = new KrakenOrderBookPersisterProcessor(BATCH_SIZE);
        (new KrakenOrderbookPersisterDriver(alertProvider, postfix, currencyPair, processor)).execute();
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

    // TODO: perhaps refactor into AbstractKrakenPersisterDriver or something like that
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
