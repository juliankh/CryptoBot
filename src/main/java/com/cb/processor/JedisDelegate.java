package com.cb.processor;

import com.cb.common.ObjectConverter;
import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.OrderBookBatch;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

@Slf4j
public class JedisDelegate {

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private Jedis jedis;

    public void insertBatch(OrderBookBatch<CbOrderBook> batch) {
        List<CbOrderBook> orderBooks = batch.getOrderbooks();
        CurrencyPair currencyPair = batch.getCurrencyPair();
        Map<String, Double> redisPayloadMap = TimeUtils.runTimedCallable_MapOutput(() -> objectConverter.convertToRedisPayload(orderBooks), "Converting [" + NumberUtils.numberFormat(orderBooks.size()) + " " + currencyPair + " CbOrderBooks] -> Map of", "CbOrderBook Redis Payload");
        long numInserted = TimeUtils.runTimedCallable_NumberedOutput(() -> jedis.zadd(currencyPair.toString(), redisPayloadMap), "Inserting into Redis", currencyPair + " Kraken CbOrderBook");
        String allOrPartialIndicator = numInserted == redisPayloadMap.size() ? "ALL" : "PARTIAL";
        log.info("Inserted [" + NumberUtils.numberFormat(numInserted) + "] out of [" + NumberUtils.numberFormat(redisPayloadMap.size()) + "] [" + currencyPair + "] OrderBooks into Redis (" + allOrPartialIndicator + ")");
    }

    public void cleanup() {
        log.info("Cleaning up");
        jedis.close();
    }

}
