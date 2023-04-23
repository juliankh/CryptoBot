package com.cb.jms.common;

import com.cb.property.CryptoProperties;
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

    public AbstractJmsComponent(String destination) {
        try {
            this.connection = connectionFactory().newConnection();
            this.channel = connection.createChannel();
        } catch (Exception e) {
            cleanup();
            throw new RuntimeException("Problem during initialization when trying to setup JMS Component for destination [" + destination + "]", e);
        }
        this.destination = destination;
    }

    @SneakyThrows
    public void cleanup() {
        if (connection != null) {
            try {
                connection.close();
            } catch (Exception e) {
                log.error("Problem while closing JMS Connection.  Logging, but otherwise ignoring.", e);
            }
        }
        if (channel != null) {
            try {
                channel.close();
            } catch (Exception e) {
                log.error("Problem while closing JMS Channel.  Logging, but otherwise ignoring.", e);
            }
        }
    }

    private ConnectionFactory connectionFactory() {
        CryptoProperties properties = new CryptoProperties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.getJmsBrokerHost());
        factory.setPort(properties.getJmsBrokerPort());
        factory.setUsername(properties.getJmsUsername());
        factory.setPassword(properties.getJmsPassword());
        return factory;
    }

}
