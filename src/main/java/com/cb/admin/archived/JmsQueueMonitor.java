package com.cb.admin.archived;

import com.cb.alert.AlertProvider;
import com.cb.db.DbReadOnlyProvider;
import com.cb.db.DbWriteProvider;
import com.cb.model.config.QueueMonitorConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.rabbitmq.http.client.Client;
import com.rabbitmq.http.client.domain.QueueInfo;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.cb.injection.BindingName.JMS_VHOST;

@Slf4j
@Singleton
public class JmsQueueMonitor {

    @Inject
    private Client jmsClient;

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private DbWriteProvider dbWriteProvider;

    @Inject
    private AlertProvider alertProvider;

    @Inject
    @Named(JMS_VHOST)
    private String jmsVhost;

    public void monitor() {
        List<QueueMonitorConfig> queueMonitorConfigs = dbReadOnlyProvider.retrieveQueueMonitorConfig();
        log.info("Configs:\n\t" + queueMonitorConfigs.parallelStream().map(Object::toString).sorted().collect(Collectors.joining("\n\t")));
        queueMonitorConfigs.parallelStream().forEach(this::monitorQueue);
    }

    public void monitorQueue(QueueMonitorConfig config) {
        String queue = config.getQueueName();
        int maxNumMessages = config.getMessageLimit();
        QueueInfo queueInfo = jmsClient.getQueue(jmsVhost, queue);
        long messages = queueInfo.getTotalMessages();
        long consumers = queueInfo.getConsumerCount();
        monitorQueue(queue, messages, maxNumMessages);
        dbWriteProvider.insertJmsDestinationStats(queue, Instant.now(), messages, consumers);
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

    public void cleanup() {
        log.info("Cleaning up");
        dbReadOnlyProvider.cleanup();
        dbWriteProvider.cleanup();
    }

}
