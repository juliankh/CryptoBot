package com.cb.jms.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbWriteProvider;
import com.cb.jms.common.AbstractJmsConsumer;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.util.Collection;

@Slf4j
public class KrakenOrderBookPersistJmsConsumer extends AbstractJmsConsumer {

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private DbWriteProvider dbWriteProvider;

    @Override
    protected void customProcess(byte[] payload) {
        KrakenOrderBookBatch batch = SerializationUtils.deserialize(payload);
        CurrencyPair batchCurrencyPair = batch.getCurrencyPair();
        Collection<DbKrakenOrderBook> orderBooks = batch.getOrderbooks().parallelStream().map(orderbook -> objectConverter.convertToKrakenOrderBook(orderbook, dbWriteProvider.getWriteConnection())).toList();
        Instant start = Instant.now();
        dbWriteProvider.insertKrakenOrderBooks(orderBooks, batchCurrencyPair);
        Instant end = Instant.now();
        double insertRate = TimeUtils.ratePerSecond(start, end, orderBooks.size());
        String currencyPairToken = currencyResolver.upperCaseToken(batchCurrencyPair, "-");
        log.info("Inserting [" + orderBooks.size() + "] [" + currencyPairToken + "] OrderBooks into db took [" + TimeUtils.durationMessage(start) + "] at a rate of [" + NumberUtils.numberFormat(insertRate) + "] items/sec");
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbWriteProvider.cleanup();
        super.cleanup();
    }

}
