package com.cb.injection.provider;

import com.cb.injection.module.MainModule;
import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Provider;

@Deprecated
@Slf4j
public class KrakenOrderBookPersistJmsConsumerProvider implements Provider<KrakenOrderBookPersistJmsConsumer> {

    private final String queue;

    public KrakenOrderBookPersistJmsConsumerProvider(String queue) {
        this.queue = queue;
    }

    @Override
    public KrakenOrderBookPersistJmsConsumer get() {
        KrakenOrderBookPersistJmsConsumer consumer = MainModule.INJECTOR.getInstance(KrakenOrderBookPersistJmsConsumer.class);
        consumer.initialize(queue);
        return consumer;
    }

}