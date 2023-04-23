package com.cb.jms.common;

import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class JmsPublisher<T extends Serializable> extends AbstractJmsComponent {

    private final String exchange;

    public JmsPublisher(String destination, String exchange) {
        super(destination);
        this.exchange = exchange;
    }

    @SneakyThrows
    public void publish(T payload) {
        channel.basicPublish(exchange, destination, null, SerializationUtils.serialize(payload));
    }

}
