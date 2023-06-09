package com.cb.processor.kraken;

import com.cb.common.ObjectConverter;
import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.KrakenBatch;
import com.cb.model.kraken.jms.XchangeKrakenOrderBook;
import com.cb.processor.BatchProcessor;
import com.cb.processor.JedisDelegate;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class XchangeKrakenOrderBookBridgeProcessor {

    @Inject
    private BatchProcessor<XchangeKrakenOrderBook, KrakenBatch<XchangeKrakenOrderBook>> batchProcessor;

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private JedisDelegate jedisDelegate;

    public void initialize(int batchSize) {
        batchProcessor.initialize(batchSize);
    }

    public void process(OrderBook orderBook, CurrencyPair currencyPair, String process) {
        if (CollectionUtils.isNotEmpty(orderBook.getBids()) && CollectionUtils.isNotEmpty(orderBook.getAsks())) {
            batchProcessor.process(
                    new XchangeKrakenOrderBook().setProcess(process).setMicroSeconds(TimeUtils.currentMicros()).setOrderBook(orderBook),
                    (List<XchangeKrakenOrderBook> orderbooks) -> new KrakenBatch<>(currencyPair, orderbooks),
                    this::processBatch);
        } else {
            log.warn("Received book with either empty {} bids or empty {} asks, so ignoring it", orderBook.getBids().size(), orderBook.getAsks().size());
        }
    }

    public void processBatch(KrakenBatch<XchangeKrakenOrderBook> krakenBatch) {
        List<XchangeKrakenOrderBook> krakenOrderBooks = krakenBatch.getOrderbooks();
        CurrencyPair currencyPair = krakenBatch.getCurrencyPair();
        List<CbOrderBook> orderBooks = TimeUtils.runTimedCallable_CollectionOutput(() -> objectConverter.convertToCbOrderBooks(krakenOrderBooks), "Converting [" + NumberUtils.numberFormat(krakenOrderBooks.size()) + " " + currencyPair + " KrakenOrderBooks] ->", "CbOrderBook");
        jedisDelegate.insertBatch(new KrakenBatch<>(currencyPair, orderBooks));
    }

    public void cleanup() {
        log.info("Cleaning up");
        jedisDelegate.cleanup();
    }

}
