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

import static java.util.Map.entry;

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
        ConnectionFactory connectionFactory = connectionFactory();
        Map<String, Integer> queueToMaxNumMessagesMap = queueMonitoringConfig();
        try (Connection connection = connectionFactory.newConnection(); Channel channel = connection.createChannel()) {
            queueToMaxNumMessagesMap.entrySet().parallelStream().forEach(entry -> monitorQueue(channel, entry.getKey(), entry.getValue()));
        }
    }

    private ConnectionFactory connectionFactory() {
        CryptoProperties properties = new CryptoProperties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.jmsBrokerHost());
        factory.setPort(properties.jmsBrokerPort());
        factory.setUsername(properties.jmsUsername());
        factory.setPassword(properties.jmsPassword());
        return factory;
    }

    @SneakyThrows
    public void monitorQueue(Channel channel, String queue, int maxNumMessages) {
        AMQP.Queue.DeclareOk result = channel.queueDeclarePassive(queue);
        int messages = result.getMessageCount(); // TODO: this is inaccurate, so figure out a different way of getting this
        int consumers = result.getConsumerCount();
        monitorQueue(queue, messages, maxNumMessages);
        dbProvider.insertJmsDestinationStats(queue, Instant.now(), messages, consumers);
    }

    @SneakyThrows
    public void monitorQueue(String queue, int messages, int maxNumMessages) {
        if (messages > maxNumMessages) {
            String msg = "For queue [" + queue + "] the current num of messages [" + messages + "] is > limit of [" + maxNumMessages + "]";
            log.warn(msg);
            alertProvider.sendEmailAlert(msg, msg);
        } else {
            log.info("For queue [" + queue + "] the current num of messages [" + messages + "] is within limit of [" + maxNumMessages + "]");
        }
    }

    // Queue name -> Max # of messages
    public Map<String, Integer> queueMonitoringConfig() {
        CryptoProperties properties = new CryptoProperties();
        return Map.ofEntries(
                entry(properties.jmsKrakenOrderBookSnapshotQueueName(), 30),
                entry(properties.jmsKrakenOrderBookSnapshotErrorQueueName(), 0)
        );
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
