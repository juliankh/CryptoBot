package com.cb.driver.kraken;

import com.cb.alert.AlertProvider;
import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.db.DbProvider;
import com.cb.driver.AbstractDriver;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class KrakenOrderBookPersisterDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Kraken OrderBook Persister";
    private static final int NUM_CONSUMERS = 20;

    private final List<KrakenOrderBookPersistJmsConsumer> consumers;

    public static void main(String[] args) {
        CurrencyResolver currencyResolver = new CurrencyResolver();
        ObjectConverter objectConverter = new ObjectConverter();
        List<KrakenOrderBookPersistJmsConsumer> consumers = new ArrayList<>();
        log.info("Number of consumers: " + NUM_CONSUMERS);
        IntStream.range(0, NUM_CONSUMERS).forEach(i -> consumers.add(new KrakenOrderBookPersistJmsConsumer(currencyResolver, objectConverter, new DbProvider())));
        AlertProvider alertProvider = new AlertProvider();
        (new KrakenOrderBookPersisterDriver(consumers, alertProvider)).execute();
    }

    @SneakyThrows
    public KrakenOrderBookPersisterDriver(List<KrakenOrderBookPersistJmsConsumer> consumers, AlertProvider alertProvider) {
        super(alertProvider);
        this.consumers = consumers;
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
