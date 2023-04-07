package com.cb.processor.kraken;

import com.cb.db.DbProvider;
import com.cb.db.ObjectConverter;
import com.cb.model.orderbook.DbKrakenOrderbook;
import com.cb.processor.BatchProcessor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.math.BigDecimal;
import java.util.Collection;

@Slf4j
public class KrakenOrderBookPersisterProcessor {

    private final ObjectConverter objectConverter;
    private final BatchProcessor<DbKrakenOrderbook> batchProcessor;

    private final DbProvider dbProvider;

    public KrakenOrderBookPersisterProcessor(int batchSize) {
        this.objectConverter = new ObjectConverter();
        this.batchProcessor = new BatchProcessor<>(batchSize);
        this.dbProvider = new DbProvider();
    }

    // TODO: make this run asynchronously somehow
    public void process(OrderBook orderBook, CurrencyPair currencyPair, String process) {
        log.trace("Received book with {} bids and {} asks", orderBook.getBids().size(), orderBook.getAsks().size());
        if (CollectionUtils.isNotEmpty(orderBook.getBids()) && CollectionUtils.isNotEmpty(orderBook.getAsks())) {
            BigDecimal bestBid = orderBook.getBids().iterator().next().getLimitPrice();
            BigDecimal bestAsk = orderBook.getAsks().iterator().next().getLimitPrice();
            if (bestBid.compareTo(bestAsk) > 0) {
                log.warn("Crossed {} book, best bid {}, best ask {}", currencyPair, bestBid, bestAsk);
            }
            DbKrakenOrderbook convertedOrderBook = objectConverter.convertToKrakenOrderBook(orderBook, dbProvider.getReadConnection(), process);
            batchProcessor.process(convertedOrderBook, (Collection<DbKrakenOrderbook> orderBooks) -> dbProvider.insertKrakenOrderBooks(orderBooks, currencyPair));
        } else {
            log.warn("Received book with either empty {} bids or empty {} asks", orderBook.getBids().size(), orderBook.getAsks().size());
        }
    }
}
