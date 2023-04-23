package com.cb.driver.kraken;

import com.cb.driver.AbstractDriver;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class KrakenOrderBookPersisterDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Kraken OrderBook Persister";

    private final KrakenOrderBookPersistJmsConsumer consumer;

    public static void main(String[] args) throws IOException {
        KrakenOrderBookPersistJmsConsumer consumer = new KrakenOrderBookPersistJmsConsumer();
        (new KrakenOrderBookPersisterDriver(consumer)).execute();
    }

    @SneakyThrows
    public KrakenOrderBookPersisterDriver(KrakenOrderBookPersistJmsConsumer consumer) {
        super();
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
