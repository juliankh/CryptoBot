package com.cb.processor.kraken;

import com.cb.common.ObjectConverter;
import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.cb.processor.BatchProcessor;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

@Slf4j
public class KrakenOrderBookBridgeProcessor {

    @Inject
    private BatchProcessor<KrakenOrderBook, KrakenOrderBookBatch> batchProcessor;

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private Jedis jedis;

    public void initialize(int batchSize) {
        batchProcessor.initialize(batchSize);
    }

    public void process(OrderBook orderBook, CurrencyPair currencyPair, String process) {
        if (CollectionUtils.isNotEmpty(orderBook.getBids()) && CollectionUtils.isNotEmpty(orderBook.getAsks())) {
            batchProcessor.process(
                    new KrakenOrderBook().setProcess(process).setMicroSeconds(TimeUtils.currentMicros()).setOrderBook(orderBook),
                    (List<KrakenOrderBook> orderbooks) -> new KrakenOrderBookBatch(currencyPair, orderbooks),
                    this::processBatch);
        } else {
            log.warn("Received book with either empty {} bids or empty {} asks, so ignoring it", orderBook.getBids().size(), orderBook.getAsks().size());
        }
    }

    public void processBatch(KrakenOrderBookBatch batch) {
        List<KrakenOrderBook> krakenOrderBooks = batch.getOrderbooks();
        CurrencyPair currencyPair = batch.getCurrencyPair();
        List<CbOrderBook> orderBooks = TimeUtils.runTimedCallable_CollectionOutput(() -> objectConverter.convertToCbOrderBooks(krakenOrderBooks), "Converting [" + NumberUtils.numberFormat(krakenOrderBooks.size()) + " " + currencyPair + " KrakenOrderBooks] ->", "CbOrderBook");
        Map<String, Double> redisPayloadMap = TimeUtils.runTimedCallable_MapOutput(() -> objectConverter.convertToRedisPayload(orderBooks), "Converting [" + NumberUtils.numberFormat(orderBooks.size()) + " " + currencyPair + " CbOrderBooks] -> Map of", "Kraken CbOrderBook Redis Payload");
        long numInserted = TimeUtils.runTimedCallable_NumberedOutput(() -> jedis.zadd(currencyPair.toString(), redisPayloadMap), "Inserting into Redis", currencyPair + " Kraken CbOrderBook");
        String allOrPartialIndicator = numInserted == redisPayloadMap.size() ? "ALL" : "PARTIAL";
        log.info("Inserted [" + NumberUtils.numberFormat(numInserted) + "] out of [" + NumberUtils.numberFormat(redisPayloadMap.size()) + "] [" + currencyPair + "] OrderBooks into Redis (" + allOrPartialIndicator + ")");
    }

    public void cleanup() {
        log.info("Cleaning up");
        jedis.close();
    }

}
