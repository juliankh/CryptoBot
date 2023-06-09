package com.cb.jms.common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public abstract class AbstractJmsComponent {

    @Inject
    private ConnectionFactory jmsConnectionFactory;

    protected Connection connection;
    protected Channel channel;
    protected String destination;

    public void initialize(String destination) {
        try {
            this.connection = jmsConnectionFactory.newConnection();
            this.channel = connection.createChannel();
        } catch (Exception e) {
            cleanup();
            throw new RuntimeException("Problem during initialization when trying to setup JMS Component for destination [" + destination + "]", e);
        }
        this.destination = destination;
    }

    @SneakyThrows
    public void cleanup() {
        log.info("Cleaning up");
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

}
