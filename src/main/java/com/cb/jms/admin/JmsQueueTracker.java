package com.cb.jms.admin;

import com.cb.db.DbProvider;
import com.cb.property.CryptoProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Set;

@Slf4j
public class JmsQueueTracker {

    private final DbProvider dbProvider;

    public JmsQueueTracker(DbProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    @SneakyThrows
    public void track() {
        CryptoProperties properties = new CryptoProperties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.jmsBrokerHost());
        factory.setPort(properties.jmsBrokerPort());
        factory.setUsername(properties.jmsUsername());
        factory.setPassword(properties.jmsPassword());
        Set<String> queuesToMonitor = properties.queuesToMonitor();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            queuesToMonitor.parallelStream().forEach(queue -> trackQueue(channel, queue));
        }
    }

    @SneakyThrows
    private void trackQueue(Channel channel, String queue) {
        AMQP.Queue.DeclareOk result = channel.queueDeclarePassive(queue);
        int messages = result.getMessageCount();
        int consumers = result.getConsumerCount();
        log.info("Queue [" + queue + "]: Messages [" + messages + "], Consumers [" + consumers + "]");
        dbProvider.insertJmsDestinationStats(queue, Instant.now(), messages, consumers);
    }

}
