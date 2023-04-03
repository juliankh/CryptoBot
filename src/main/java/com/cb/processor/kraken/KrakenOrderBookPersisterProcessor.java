package com.cb.processor.kraken;

import com.cb.db.DbProvider;
import com.cb.processor.BatchProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.math.BigDecimal;
import java.util.Collection;

@Slf4j
public class KrakenOrderBookPersisterProcessor {

    private static final int BATCH_SIZE = 300;

    private final BatchProcessor<OrderBook> batchProcessor;

    private final DbProvider dbProvider;

    public KrakenOrderBookPersisterProcessor() {
        this.batchProcessor = new BatchProcessor<>(BATCH_SIZE);
        this.dbProvider = new DbProvider();
    }

    // !!!!!!!!!!!!! TODO: check if there are any differences in time gaps b/w orderbook snapshots in order to deduce if while persisting some orderbooks get missed (do websocket updates that aren't processed right away get dropped?)

    // TODO: unit test
    public void process(OrderBook orderBook, CurrencyPair currencyPair, String process) {
        log.trace("Received book with {} bids and {} asks", orderBook.getBids().size(), orderBook.getAsks().size());
        if (CollectionUtils.isNotEmpty(orderBook.getBids()) && CollectionUtils.isNotEmpty(orderBook.getAsks())) {
            BigDecimal bestBid = orderBook.getBids().iterator().next().getLimitPrice();
            BigDecimal bestAsk = orderBook.getAsks().iterator().next().getLimitPrice();
            if (bestBid.compareTo(bestAsk) > 0) {
                log.warn("Crossed {} book, best bid {}, best ask {}", currencyPair, bestBid, bestAsk);
            }
            batchProcessor.process(orderBook, (Collection<OrderBook> orderBooks) -> dbProvider.insertKrakenOrderBooks(orderBooks, currencyPair, process));
        } else {
            log.warn("Received book with either empty {} bids or empty {} asks", orderBook.getBids().size(), orderBook.getAsks().size());
        }
    }
}
