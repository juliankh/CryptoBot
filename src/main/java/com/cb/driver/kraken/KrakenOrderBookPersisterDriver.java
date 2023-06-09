package com.cb.driver.kraken;

import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

@Deprecated
@Slf4j
public class KrakenOrderBookPersisterDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Kraken OrderBook Persister";

    @Inject
    private List<KrakenOrderBookPersistJmsConsumer> consumers;

    public static void main(String[] args) {
        KrakenOrderBookPersisterDriver driver = MainModule.INJECTOR.getInstance(KrakenOrderBookPersisterDriver.class);
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
