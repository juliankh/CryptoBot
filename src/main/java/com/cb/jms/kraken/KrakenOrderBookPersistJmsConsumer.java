package com.cb.jms.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.jms.common.AbstractJmsConsumer;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.knowm.xchange.currency.CurrencyPair;
import redis.clients.jedis.Jedis;

import java.util.List;
import java.util.Map;

@Deprecated
@Slf4j
public class KrakenOrderBookPersistJmsConsumer extends AbstractJmsConsumer {

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private Jedis jedis;

    @Override
    protected void customProcess(byte[] payload) {
        KrakenOrderBookBatch batch = TimeUtils.runTimedCallable_ObjectOutput(() -> SerializationUtils.deserialize(payload), "Deserializing Kraken OrderBook Jms payload");
        CurrencyPair batchCurrencyPair = batch.getCurrencyPair();
        List<KrakenOrderBook> krakenOrderBooks = batch.getOrderbooks();
        List<CbOrderBook> orderBooks = TimeUtils.runTimedCallable_CollectionOutput(() -> objectConverter.convertToCbOrderBooks(krakenOrderBooks), "Converting [" + NumberUtils.numberFormat(krakenOrderBooks.size()) + " " + batchCurrencyPair + " KrakenOrderBooks] ->", "CbOrderBook");
        Map<String, Double> redisPayloadMap = TimeUtils.runTimedCallable_MapOutput(() -> objectConverter.convertToRedisPayload(orderBooks), "Converting [" + NumberUtils.numberFormat(orderBooks.size()) + " " + batchCurrencyPair + " CbOrderBooks] -> Map of", "Kraken CbOrderBook Redis Payload");
        long numInserted = TimeUtils.runTimedCallable_NumberedOutput(() -> jedis.zadd(batchCurrencyPair.toString(), redisPayloadMap), "Inserting into Redis", batchCurrencyPair + " Kraken CbOrderBook");
        String allOrPartialIndicator = numInserted == redisPayloadMap.size() ? "ALL" : "PARTIAL";
        log.info("Inserted [" + NumberUtils.numberFormat(numInserted) + "] out of [" + NumberUtils.numberFormat(redisPayloadMap.size()) + "] [" + batchCurrencyPair + "] OrderBooks into Redis (" + allOrPartialIndicator + ")");
    }

    public void cleanup() {
        log.info("Cleaning up");
        jedis.close();
        super.cleanup();
    }

}
