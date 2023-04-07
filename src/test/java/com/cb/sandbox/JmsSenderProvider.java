package com.cb.sandbox;

import com.cb.model.jms.KrakenOrderBookBatch;
import com.cb.model.orderbook.DbKrakenOrderbook;
import com.cb.property.CryptoProperties;
import com.cb.util.TimeUtils;
import com.google.common.collect.Lists;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.SerializationUtils;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class JmsSenderProvider {

    public static void main(String[] args) throws IOException, TimeoutException {
        CryptoProperties props = new CryptoProperties();

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(props.getJmsBrokerHost());
        factory.setPort(props.getJmsBrokerPort());
        factory.setUsername(props.getJmsUsername());
        factory.setPassword(props.getJmsPassword());

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();


        try (connection; channel) {
            String message = "Hello World!";

            long receivedNanos = TimeUtils.currentNanos();
            DbKrakenOrderbook orderbook1 = new DbKrakenOrderbook();orderbook1.setReceived_nanos(receivedNanos);orderbook1.setBids_hash(111);orderbook1.setAsks_hash(1111);orderbook1.setId(123L);
            DbKrakenOrderbook orderbook2 = new DbKrakenOrderbook();orderbook2.setReceived_nanos(receivedNanos);orderbook2.setBids_hash(222);orderbook2.setAsks_hash(2222);orderbook2.setId(124L);
            DbKrakenOrderbook orderbook3 = new DbKrakenOrderbook();orderbook3.setReceived_nanos(receivedNanos);orderbook3.setBids_hash(333);orderbook3.setAsks_hash(3333);orderbook3.setId(125L);
            DbKrakenOrderbook orderbook4 = new DbKrakenOrderbook();orderbook4.setReceived_nanos(receivedNanos);orderbook4.setBids_hash(444);orderbook4.setAsks_hash(4444);orderbook4.setId(126L);
            DbKrakenOrderbook orderbook5 = new DbKrakenOrderbook();orderbook5.setReceived_nanos(receivedNanos);orderbook5.setBids_hash(555);orderbook5.setAsks_hash(5555);orderbook5.setId(127L);
            List<DbKrakenOrderbook> orderbooks = Lists.newArrayList(orderbook1, orderbook2, orderbook3, orderbook4, orderbook5);

            //channel.basicPublish("", props.getJmsTopicOrderBookSnapshotBtcUsd(), null, message.getBytes(StandardCharsets.UTF_8));
            channel.basicPublish("", props.getJmsTopicOrderBookSnapshotBtcUsd(), null, SerializationUtils.serialize(new KrakenOrderBookBatch(CurrencyPair.BTC_USD, orderbooks)));

            System.out.println(" [x] Sent '" + message + "'");
        }
    }

}
