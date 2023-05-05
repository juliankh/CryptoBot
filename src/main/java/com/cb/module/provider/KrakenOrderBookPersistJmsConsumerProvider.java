package com.cb.module.provider;

import com.cb.jms.kraken.KrakenOrderBookPersistJmsConsumer;
import com.cb.module.CryptoBotModule;
import com.google.inject.Provider;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class KrakenOrderBookPersistJmsConsumerProvider implements Provider<KrakenOrderBookPersistJmsConsumer> {

    private final String queue;

    public KrakenOrderBookPersistJmsConsumerProvider(String queue) {
        this.queue = queue;
    }

    @Override
    public KrakenOrderBookPersistJmsConsumer get() {
        KrakenOrderBookPersistJmsConsumer consumer = CryptoBotModule.INJECTOR.getInstance(KrakenOrderBookPersistJmsConsumer.class);
        consumer.initialize(queue);
        return consumer;
    }

}