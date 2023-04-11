package com.cb.jms.kraken;

import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import com.cb.db.ObjectConverter;
import com.cb.jms.common.AbstractJmsConsumer;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.cb.property.CryptoProperties;
import com.cb.util.CurrencyResolver;
import com.cb.util.TimeUtils;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
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

    public KrakenOrderBookPersistJmsConsumer(AlertProvider alertProvider) {
        this(connectionFactory(), destination(), alertProvider);
    }

    public KrakenOrderBookPersistJmsConsumer(ConnectionFactory factory, String destination, AlertProvider alertProvider) {
        super(factory, destination, alertProvider);
        this.tokenResolver = new CurrencyResolver();
        this.objectConverter = new ObjectConverter();
        this.dbProvider = new DbProvider();
    }

    private static ConnectionFactory connectionFactory() {
        CryptoProperties properties = properties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getJmsBrokerHost());
        factory.setPort(properties.getJmsBrokerPort());
        factory.setUsername(properties.getJmsUsername());
        factory.setPassword(properties.getJmsPassword());
        return factory;
    }

    private static String destination() {
        CryptoProperties properties = properties();
        return properties.getJmsKrakenOrderBookSnapshotQueueName();
    }

    @SneakyThrows
    private static CryptoProperties properties() {
        return new CryptoProperties();
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
