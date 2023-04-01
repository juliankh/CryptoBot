package com.cb.driver.kraken;

import com.cb.alert.AlertProviderImpl;
import com.cb.driver.AbstractDriver;
import com.cb.processor.kraken.KrakenOrderBookPersisterProcessor;
import com.cb.util.CryptoUtils;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.reactivex.disposables.Disposable;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;

@Slf4j
public class KrakenOrderbookPersisterDriver extends AbstractDriver {

    private static final int SLEEP_SECS = 2;

    private static final int ORDER_BOOK_DEPTH = 500;

    private static Throwable THROWABLE = null;

    private final KrakenOrderBookPersisterProcessor processor;

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
        krakenExchange.connect().blockingAwait();
        Disposable disposable = subscribe(krakenExchange, CurrencyPair.BTC_USDT);
        while (true) {
            if (disposable.isDisposed()) {
                throw new RuntimeException("Process [" + getDriverName() + "] unexpectedly stopped: " + THROWABLE.getMessage(), THROWABLE);
            }
            CryptoUtils.sleepQuietlyForSecs(SLEEP_SECS);
        }
    }

    public Disposable subscribe(StreamingExchange krakenExchange, CurrencyPair currencyPair) {
        log.info("Subscribing for [" + currencyPair + "]");
        return krakenExchange
                    .getStreamingMarketDataService()
                    .getOrderBook(currencyPair, ORDER_BOOK_DEPTH)
                    .onTerminateDetach()
                    .subscribe(
                            orderBook -> processor.process(orderBook, currencyPair),
                            throwable -> {
                                log.error("Failed to get OrderBook: {}", throwable.getMessage(), throwable);
                                krakenExchange.disconnect().subscribe(() -> log.info("Disconnected!"));
                                THROWABLE = throwable;
                            }
                    );
    }

}
