package com.cb.jms.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbProvider;
import com.cb.jms.common.AbstractJmsConsumer;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.cb.property.CryptoProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.SerializationUtils;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.util.Collection;

@Slf4j
public class KrakenOrderBookPersistJmsConsumer extends AbstractJmsConsumer {

    private final CurrencyResolver currencyResolver;
    private final ObjectConverter objectConverter;
    private final DbProvider dbProvider;

    public KrakenOrderBookPersistJmsConsumer(CurrencyResolver currencyResolver, ObjectConverter objectConverter, DbProvider dbProvider) {
        super(destination());
        this.currencyResolver = currencyResolver;
        this.objectConverter = objectConverter;
        this.dbProvider = dbProvider;
    }

    private static String destination() {
        CryptoProperties properties = new CryptoProperties();
        return properties.jmsKrakenOrderBookSnapshotQueueName();
    }

    @Override
    protected void customProcess(byte[] payload) {
        KrakenOrderBookBatch batch = SerializationUtils.deserialize(payload);
        CurrencyPair batchCurrencyPair = batch.getCurrencyPair();
        Collection<DbKrakenOrderBook> orderBooks = batch.getOrderbooks().parallelStream().map(orderbook -> objectConverter.convertToKrakenOrderBook(orderbook, dbProvider.getReadConnection())).toList();
        Instant start = Instant.now();
        dbProvider.insertKrakenOrderBooks(orderBooks, batchCurrencyPair);
        Instant end = Instant.now();
        double insertRate = TimeUtils.ratePerSecond(start, end, orderBooks.size());
        String currencyPairToken = currencyResolver.upperCaseToken(batchCurrencyPair, "-");
        log.info("Inserting [" + orderBooks.size() + "] [" + currencyPairToken + "] OrderBooks into db took [" + TimeUtils.durationMessage(start) + "] at a rate of [" + NumberUtils.NUMBER_FORMAT.format(insertRate) + "] items/sec");
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
