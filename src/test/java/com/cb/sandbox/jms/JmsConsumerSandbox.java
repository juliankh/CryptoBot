package com.cb.sandbox.jms;

import com.cb.model.kraken.jms.KrakenOrderBookBatch;
import com.cb.property.CryptoProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;
import org.apache.commons.lang3.SerializationUtils;

public class JmsConsumerSandbox {

    public static void main(String[] argv) throws Exception {

        CryptoProperties properties = new CryptoProperties();
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(properties.jmsBrokerHost());
        factory.setPort(properties.jmsBrokerPortAmqp());
        factory.setUsername(properties.jmsUsername());
        factory.setPassword(properties.jmsPassword());

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        DeliverCallback deliverCallback = (consumerTag, delivery) -> {
            //String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            //System.out.println(" [x] Received '" + message + "'");
            KrakenOrderBookBatch batch = SerializationUtils.deserialize(delivery.getBody());
            System.out.println(" [x] Received '" + batch + "'");
        };
        channel.basicConsume("orderbook_snapshot_persist_kraken_btc_usd", true,
                deliverCallback,
                consumerTag -> {}
        );
    }

}
