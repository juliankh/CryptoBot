package com.cb.jms.common;

import com.cb.alert.Alerter;
import com.cb.common.util.TimeUtils;
import com.rabbitmq.client.ConsumerShutdownSignalCallback;
import com.rabbitmq.client.DeliverCallback;
import com.rabbitmq.client.ShutdownSignalException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;

@Slf4j
public abstract class AbstractJmsConsumer extends AbstractJmsComponent {

    @Inject
    private Alerter alerter;

    private int numMessagesReceived = 0;

    @SneakyThrows
    public void engageConsumption() {
        channel.basicConsume(destination, false, deliverCallback(), shutdownCallback());
        log.info("Started listening to destination [" + destination + "]");
    }

    protected abstract void customProcess(byte[] paylod);

    protected DeliverCallback deliverCallback() {
        return (consumerTag, delivery) -> {
            long deliveryTag = delivery.getEnvelope().getDeliveryTag();
            try {
                Instant start = Instant.now();
                log.info("Received msg [" + ++numMessagesReceived + "] with Delivery Tag [" + deliveryTag + "]");
                customProcess(delivery.getBody());
                log.info("Processing msg took [" + TimeUtils.durationMessage(start) + "] ------------------------");
                channel.basicAck(deliveryTag, false);
            } catch (Exception e) {
                log.error("Problem processing msg", e);
                channel.basicNack(deliveryTag, false, false);
            }
        };
    }

    protected ConsumerShutdownSignalCallback shutdownCallback() {
        return (String consumerTag, ShutdownSignalException e) -> {
            log.info("Received Shutdown Signal for ConsumerTag [" + consumerTag + "]", e);
            cleanup();
            String msg = "JMS listener shut down for [" + destination + "]";
            alerter.sendEmailAlertQuietly(msg, msg, e);
        };
    }

}
