package com.cb.jms.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import com.cb.property.CryptoProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

@Slf4j
public class JmsQueueMonitor {

    private final DbProvider dbProvider;
    private final AlertProvider alertProvider;

    public JmsQueueMonitor(DbProvider dbProvider, AlertProvider alertProvider) {
        this.dbProvider = dbProvider;
        this.alertProvider = alertProvider;
    }

    @SneakyThrows
    public void monitor() {
        CryptoProperties properties = new CryptoProperties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.jmsBrokerHost());
        factory.setPort(properties.jmsBrokerPort());
        factory.setUsername(properties.jmsUsername());
        factory.setPassword(properties.jmsPassword());
        Map<String, Integer> queueToMaxNumMessagesMap = properties.queueToMaxNumMessagesMap();
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            queueToMaxNumMessagesMap.entrySet().parallelStream().forEach(entry -> monitorQueue(channel, entry.getKey(), entry.getValue()));
        }
    }

    @SneakyThrows
    public void monitorQueue(Channel channel, String queue, int maxNumMessages) {
        AMQP.Queue.DeclareOk result = channel.queueDeclarePassive(queue);
        monitorQueue(result, queue, maxNumMessages);
    }

    // TODO: unit test
    @SneakyThrows
    public void monitorQueue(AMQP.Queue.DeclareOk result, String queue, int maxNumMessages) {
        int messages = result.getMessageCount();
        int consumers = result.getConsumerCount();
        if (messages > maxNumMessages) {
            String msg = "For queue [" + queue + "] the current num of messages [" + messages + "] is > limit of [" + maxNumMessages + "]";
            log.warn(msg);
            alertProvider.sendEmailAlert(msg, msg);
        } else {
            log.info("For queue [" + queue + "] the current num of messages [" + messages + "] is within limit of [" + maxNumMessages + "]");
        }
        dbProvider.insertJmsDestinationStats(queue, Instant.now(), messages, consumers);
    }

    public void cleanup() {
        dbProvider.cleanup();
    }

}
