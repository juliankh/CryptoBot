package com.cb.sandbox.jms;

import com.cb.property.CryptoProperties;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class JmsMonitorSandbox {

    public static void main(String[] argv) throws Exception {
        CryptoProperties properties = new CryptoProperties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.jmsBrokerHost());
        factory.setPort(properties.jmsBrokerPortAmqp());
        factory.setUsername(properties.jmsUsername());
        factory.setPassword(properties.jmsPassword());
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            String queueName = properties.jmsKrakenOrderBookSnapshotQueueName();
            AMQP.Queue.DeclareOk dok = channel.queueDeclarePassive(queueName);
            System.out.println(queueName + " - # messages: " + dok.getMessageCount());
            System.out.println(queueName + " - # consumers: " + dok.getConsumerCount());
        }
    }

}
