package com.cb.jms.common;

import lombok.SneakyThrows;
import org.apache.commons.lang3.SerializationUtils;

import java.io.Serializable;

public class JmsPublisher extends AbstractJmsComponent {

    private String exchange;

    public void initialize(String exchange, String destination) {
        this.exchange = exchange;
        super.initialize(destination);
    }

    @SneakyThrows
    public void publish(Serializable payload) {
        channel.basicPublish(exchange, destination, null, SerializationUtils.serialize(payload));
    }

}
