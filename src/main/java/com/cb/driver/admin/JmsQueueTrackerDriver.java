package com.cb.driver.admin;

import com.cb.db.DbProvider;
import com.cb.driver.AbstractDriver;
import com.cb.jms.admin.JmsQueueTracker;

public class JmsQueueTrackerDriver extends AbstractDriver  {

    private static final String DRIVER_NAME = "JMS Queue Tracker";

    private final JmsQueueTracker jmsQueueTracker;

    public static void main(String[] args) {
        (new JmsQueueTrackerDriver()).execute();
    }

    public JmsQueueTrackerDriver() {
        super();
        this.jmsQueueTracker = new JmsQueueTracker(new DbProvider());
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        jmsQueueTracker.track();
    }

    @Override
    protected void cleanup() {
        // nothing
    }

}
