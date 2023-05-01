package com.cb.driver.admin;

import com.cb.admin.JmsQueueMonitor;
import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import com.cb.driver.AbstractDriver;
import com.cb.property.CryptoProperties;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmsQueueMonitorDriver extends AbstractDriver  {

    private static final String DRIVER_NAME = "JMS Queue Monitor";

    private final JmsQueueMonitor jmsQueueMonitor;

    public static void main(String[] args) {
        CryptoProperties properties = new CryptoProperties();
        DbProvider dbProvider = new DbProvider();
        AlertProvider alertProvider = new AlertProvider();
        JmsQueueMonitor jmsQueueMonitor = new JmsQueueMonitor(properties, dbProvider, alertProvider);
        (new JmsQueueMonitorDriver(jmsQueueMonitor, alertProvider)).execute();
    }

    public JmsQueueMonitorDriver(JmsQueueMonitor jmsQueueMonitor, AlertProvider alertProvider) {
        super(alertProvider);
        this.jmsQueueMonitor = jmsQueueMonitor;
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        jmsQueueMonitor.monitor();
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        jmsQueueMonitor.cleanup();
    }

}
