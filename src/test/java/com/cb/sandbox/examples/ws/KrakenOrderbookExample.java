package com.cb.sandbox.examples.ws;

import static org.knowm.xchange.currency.CurrencyPair.BTC_USD;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

@Slf4j
public class KrakenOrderbookExample {

    public static void main(String[] args) throws InterruptedException {
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
        StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        krakenExchange.connect().blockingAwait();
        subscribe(krakenExchange, BTC_USD);
        TimeUnit.SECONDS.sleep(Integer.MAX_VALUE);
        krakenExchange.disconnect().subscribe(() -> log.info("Disconnected"));
    }

    private static Disposable subscribe(StreamingExchange krakenExchange, CurrencyPair currencyPair) {
        return krakenExchange
                .getStreamingMarketDataService()
                .getOrderBook(currencyPair, 500)
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
                            log.error("Fail to get OrderBook {}", throwable.getMessage(), throwable);
                        });
    }

}
