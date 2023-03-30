package com.cb.kraken;

import com.cb.alert.AlertProviderImpl;
import com.cb.driver.AbstractDriver;
import com.cb.util.CryptoUtils;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.math.BigDecimal;

@Slf4j
public class KrakenOrderbookPersisterDriver extends AbstractDriver {

    private static final int SLEEP_SECS = 2;

    private static Throwable THROWABLE = null;

    public KrakenOrderbookPersisterDriver() throws IOException {
        super(new AlertProviderImpl());
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
        Disposable disposable = subscribe(krakenExchange, CurrencyPair.BTC_USD);
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
                    .getOrderBook(currencyPair, 500)
                    .onTerminateDetach()
                    .subscribe(
                            s -> {
                                log.info("Received book with {} bids and {} asks", s.getBids().size(), s.getAsks().size());
                                if (!s.getBids().isEmpty()) {
                                    BigDecimal bestBid = s.getBids().iterator().next().getLimitPrice();
                                    BigDecimal bestAsk = s.getAsks().iterator().next().getLimitPrice();
                                    if (bestBid.compareTo(bestAsk) > 0) {
                                        log.warn("Crossed {} book, best bid {}, best ask {}", currencyPair, bestBid, bestAsk);
                                    }
                                }
                            },
                            throwable -> {
                                log.error("Failed to get OrderBook: {}", throwable.getMessage(), throwable);
                                krakenExchange.disconnect().subscribe(() -> log.info("Disconnected!"));
                                THROWABLE = throwable;
                            }
                    );
    }

}
