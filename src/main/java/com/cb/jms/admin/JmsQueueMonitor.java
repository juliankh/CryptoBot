package com.cb.jms.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import com.cb.property.CryptoProperties;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.domain.QueueInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.Map;

import static java.util.Map.entry;

@Slf4j
@RequiredArgsConstructor
public class JmsQueueMonitor {

    private final CryptoProperties properties;
    private final DbProvider dbProvider;
    private final AlertProvider alertProvider;

    public static void main(String[] args) {
        (new JmsQueueMonitor(new CryptoProperties(), new DbProvider(), new AlertProvider())).monitor();
    }

    public void monitor() {
        Client c = client();
        Map<String, Integer> queueToMaxNumMessagesMap = queueMonitoringConfig();
        queueToMaxNumMessagesMap.entrySet().parallelStream().forEach(entry -> monitorQueue(c, entry.getKey(), entry.getValue()));
    }

    public void monitorQueue(Client c, String queue, int maxNumMessages) {
        String vhost = properties.jmsBrokerVhost();
        QueueInfo queueInfo = c.getQueue(vhost, queue);
        long messages = queueInfo.getTotalMessages();
        long consumers = queueInfo.getConsumerCount();
        monitorQueue(queue, messages, maxNumMessages);
        dbProvider.insertJmsDestinationStats(queue, Instant.now(), messages, consumers);
    }

    public void monitorQueue(String queue, long messages, long maxNumMessages) {
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
                entry(properties.jmsKrakenOrderBookSnapshotQueueName(), 100),
                entry(properties.jmsKrakenOrderBookSnapshotErrorQueueName(), 0)
        );
    }

    @SneakyThrows
    public Client client() {
        String host = properties.jmsBrokerHost();
        int port = properties.jmsBrokerPortHttp();
        String baseUrl = "http://" + host + ":" + port + "/api/";
        String username = properties.jmsUsername();
        String password = properties.jmsPassword();
        return new Client(new ClientParameters().url(baseUrl).username(username).password(password));
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
