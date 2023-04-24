package com.cb.driver.kraken;

import com.cb.alert.AlertProvider;
import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.db.DbProvider;
import com.cb.driver.AbstractDriver;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KrakenOrderBookPersisterDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Kraken OrderBook Persister";

    private final KrakenOrderBookPersistJmsConsumer consumer;

    public static void main(String[] args) {
        CurrencyResolver currencyResolver = new CurrencyResolver();
        ObjectConverter objectConverter = new ObjectConverter();
        DbProvider dbProvider = new DbProvider();
        KrakenOrderBookPersistJmsConsumer consumer = new KrakenOrderBookPersistJmsConsumer(currencyResolver, objectConverter, dbProvider);
        AlertProvider alertProvider = new AlertProvider();
        (new KrakenOrderBookPersisterDriver(consumer, alertProvider)).execute();
    }

    @SneakyThrows
    public KrakenOrderBookPersisterDriver(KrakenOrderBookPersistJmsConsumer consumer, AlertProvider alertProvider) {
        super(alertProvider);
        this.consumer = consumer;
    }

    @Override
    public String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        consumer.engageConsumption();
    }

    @Override
    protected void cleanup() {
        consumer.cleanup();
    }

}
