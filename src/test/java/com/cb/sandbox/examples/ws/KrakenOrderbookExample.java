package com.cb.sandbox.examples.ws;

import com.cb.common.util.TimeUtils;
import com.google.common.collect.Lists;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.CurrencyPair;

import java.math.BigDecimal;
import java.util.List;

import static org.knowm.xchange.currency.CurrencyPair.BTC_USD;

@Slf4j
public class KrakenOrderbookExample {

    private static final List<CurrencyPair> CURRENCY_PAIRS = Lists.newArrayList(BTC_USD);

    public static void main(String[] args) {
        (new KrakenOrderbookExample()).engage();
    }

    private void engage() {
        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
        StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
        krakenExchange.connect().blockingAwait();
        CURRENCY_PAIRS.forEach(currencyPair -> subscribe(krakenExchange, currencyPair));
        TimeUtils.sleepQuietlyForMins(Integer.MAX_VALUE);
    }

    public Disposable subscribe(StreamingExchange krakenExchange, CurrencyPair currencyPair) {
        log.info("Subscribing for [" + currencyPair + "]");
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
