package com.cb.driver.admin;

import com.cb.admin.JmsQueueMonitor;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JmsQueueMonitorDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "JMS Queue Monitor";

    @Inject
    private JmsQueueMonitor jmsQueueMonitor;

    public static void main(String[] args) {
        JmsQueueMonitorDriver driver = MainModule.INJECTOR.getInstance(JmsQueueMonitorDriver.class);
        driver.execute();
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
