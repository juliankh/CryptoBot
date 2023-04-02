package com.cb.driver.kraken;

import com.cb.alert.AlertProviderImpl;
import com.cb.driver.AbstractDriver;
import com.cb.processor.kraken.KrakenOrderBookPersisterProcessor;
import com.cb.util.TimeUtils;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.reactivex.disposables.Disposable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KrakenOrderbookPersisterDriver extends AbstractDriver {

    private static final int MAX_SECS_BETWEEN_UPDATES = 5;

    private static final int SLEEP_SECS = 2;

    private static final int ORDER_BOOK_DEPTH = 500;

    private final KrakenOrderBookPersisterProcessor processor;

    private final AtomicReference<Instant> latestReceive = new AtomicReference<>();

    private Throwable throwable;

    @SneakyThrows
    public KrakenOrderbookPersisterDriver() {
        super(new AlertProviderImpl());
        this.processor = new KrakenOrderBookPersisterProcessor();
    }

    public static void main(String[] args) throws IOException {
        (new KrakenOrderbookPersisterDriver()).execute();
    }

    @Override
    public String getDriverName() {
        return "Kraken OrderBook Persister";
    }

    @Override
    protected void executeCustom() {
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
        StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        Disposable disposable = subscribe(krakenExchange, CurrencyPair.BTC_USDT);

        // TODO: put into separate class/method
        while (true) {
            long secsSinceLastUpdate = ChronoUnit.SECONDS.between(latestReceive.get(), Instant.now());
            if (secsSinceLastUpdate > MAX_SECS_BETWEEN_UPDATES) {
                log.warn("It's been [" + secsSinceLastUpdate + "] secs since data was last received, which is above the threshold of [" + MAX_SECS_BETWEEN_UPDATES + "] secs, so will try to reconnect");
                disconnect(krakenExchange);
                TimeUtils.sleepQuietlyForSecs(15);
                subscribe(krakenExchange, CurrencyPair.BTC_USDT);
            }
            if (disposable.isDisposed()) {
                disconnect(krakenExchange);
                throw new RuntimeException("Process [" + getDriverName() + "] unexpectedly stopped", throwable);
            }
            TimeUtils.sleepQuietlyForSecs(SLEEP_SECS);
        }
    }

    public Disposable subscribe(StreamingExchange krakenExchange, CurrencyPair currencyPair) {
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
                    orderBook -> processor.process(orderBook, currencyPair),
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
