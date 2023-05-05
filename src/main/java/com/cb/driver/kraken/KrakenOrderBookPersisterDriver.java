package com.cb.driver.kraken;

import com.cb.driver.AbstractDriver;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import com.cb.module.CryptoBotModule;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

@Slf4j
public class KrakenOrderBookPersisterDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Kraken OrderBook Persister";

    @Inject
    private List<KrakenOrderBookPersistJmsConsumer> consumers;

    public static void main(String[] args) {
        KrakenOrderBookPersisterDriver driver = CryptoBotModule.INJECTOR.getInstance(KrakenOrderBookPersisterDriver.class);
        driver.execute();
    }

    @Override
    public String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        consumers.parallelStream().forEach(KrakenOrderBookPersistJmsConsumer::engageConsumption);
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        consumers.parallelStream().forEach(KrakenOrderBookPersistJmsConsumer::cleanup);
    }

}
