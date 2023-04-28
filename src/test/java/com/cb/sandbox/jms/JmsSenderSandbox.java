package com.cb.sandbox.jms;

import com.cb.common.util.TimeUtils;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.property.CryptoProperties;
import com.google.common.collect.Lists;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class JmsSenderSandbox {

    public static void main(String[] args) throws IOException, TimeoutException {
        CryptoProperties props = new CryptoProperties();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(props.jmsBrokerHost());
        factory.setPort(props.jmsBrokerPortAmqp());
        factory.setUsername(props.jmsUsername());
        factory.setPassword(props.jmsPassword());

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        try (connection; channel) {
            //String message = "Hello World!";
            //channel.basicPublish("", props.getJmsTopicOrderBookSnapshotBtcUsd(), null, message.getBytes(StandardCharsets.UTF_8));
            //System.out.println(" [x] Sent '" + message + "'");

            long receivedNanos = TimeUtils.currentNanos();
            DbKrakenOrderBook orderbook1 = new DbKrakenOrderBook();orderbook1.setReceived_nanos(receivedNanos);orderbook1.setBids_hash(111);orderbook1.setAsks_hash(1111);orderbook1.setId(123L);
            DbKrakenOrderBook orderbook2 = new DbKrakenOrderBook();orderbook2.setReceived_nanos(receivedNanos);orderbook2.setBids_hash(222);orderbook2.setAsks_hash(2222);orderbook2.setId(124L);
            DbKrakenOrderBook orderbook3 = new DbKrakenOrderBook();orderbook3.setReceived_nanos(receivedNanos);orderbook3.setBids_hash(333);orderbook3.setAsks_hash(3333);orderbook3.setId(125L);
            DbKrakenOrderBook orderbook4 = new DbKrakenOrderBook();orderbook4.setReceived_nanos(receivedNanos);orderbook4.setBids_hash(444);orderbook4.setAsks_hash(4444);orderbook4.setId(126L);
            DbKrakenOrderBook orderbook5 = new DbKrakenOrderBook();orderbook5.setReceived_nanos(receivedNanos);orderbook5.setBids_hash(555);orderbook5.setAsks_hash(5555);orderbook5.setId(127L);

            List<DbKrakenOrderBook> orderbooks = Lists.newArrayList(orderbook1, orderbook2, orderbook3, orderbook4, orderbook5);
            //KrakenOrderBookBatch batch = new KrakenOrderBookBatch(CurrencyPair.BTC_USD, orderbooks);
            //channel.basicPublish("", props.getJmsQueueOrderBookSnapshotKrakenPersist(CurrencyPair.BTC_USD), null,  SerializationUtils.serialize(batch));
        }
    }

}
