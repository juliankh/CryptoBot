package com.cb.jms.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
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

    private final CurrencyResolver tokenResolver;
    private final ObjectConverter objectConverter;
    private final DbProvider dbProvider;

    public KrakenOrderBookPersistJmsConsumer() {
        super(destination());
        this.tokenResolver = new CurrencyResolver();
        this.objectConverter = new ObjectConverter();
        this.dbProvider = new DbProvider();
    }

    private static String destination() {
        CryptoProperties properties = new CryptoProperties();
        return properties.getJmsKrakenOrderBookSnapshotQueueName();
    }

    @Override
    protected void customProcess(byte[] payload) {
        KrakenOrderBookBatch batch = SerializationUtils.deserialize(payload);
        CurrencyPair batchCurrencyPair = batch.getCurrencyPair();
        Collection<DbKrakenOrderBook> orderBooks = batch.getOrderbooks().parallelStream().map(orderbook -> objectConverter.convertToKrakenOrderBook(orderbook, dbProvider.getReadConnection())).toList();
        Instant start = Instant.now();
        dbProvider.insertKrakenOrderBooks(orderBooks, batchCurrencyPair);
        Instant end = Instant.now();
        long insertRate = TimeUtils.ratePerSecond(start, end, orderBooks.size());
        String currencyPairToken = tokenResolver.upperCaseToken(batchCurrencyPair, "-");
        log.info("Inserting [" + orderBooks.size() + "] [" + currencyPairToken + "] OrderBooks into db took [" + TimeUtils.durationMessage(start) + "] at a rate of [" + insertRate + "] items/sec");
    }

}
