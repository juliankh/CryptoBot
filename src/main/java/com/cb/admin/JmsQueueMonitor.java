package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import com.cb.model.config.QueueMonitorConfig;
import com.cb.property.CryptoProperties;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.ClientParameters;
import com.rabbitmq.http.client.domain.QueueInfo;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

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
        List<QueueMonitorConfig> queueMonitorConfigs = dbProvider.retrieveQueueMonitorConfig();
        log.info("Configs:\n\t" + queueMonitorConfigs.parallelStream().map(Object::toString).sorted().collect(Collectors.joining("\n\t")));
        queueMonitorConfigs.parallelStream().forEach(config -> monitorQueue(c, config));
    }

    public void monitorQueue(Client c, QueueMonitorConfig config) {
        String queue = config.getQueueName();
        int maxNumMessages = config.getMessageLimit();
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
