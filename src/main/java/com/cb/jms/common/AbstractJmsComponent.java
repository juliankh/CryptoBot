package com.cb.jms.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractJmsComponent {

    protected final Connection connection;
    protected final Channel channel;
    protected final String destination;

    public AbstractJmsComponent(ConnectionFactory factory, String destination) {
        try {
            this.connection = factory.newConnection();
            this.channel = connection.createChannel();
        } catch (Exception e) {
            cleanup();
            throw new RuntimeException("Problem during initialization with either the ConnectionFactory or Channel", e);
        }
        this.destination = destination;
    }

    @SneakyThrows
    public void cleanup() {
        if (connection != null) {
            connection.close();
        }
        if (channel != null) {
            channel.close();
        }
    }

}
