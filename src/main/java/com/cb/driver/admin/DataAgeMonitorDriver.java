package com.cb.driver.admin;

import com.cb.admin.RedisDataAgeMonitor;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataAgeMonitorDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Data Age Monitor";

    @Inject
    private RedisDataAgeMonitor redisDataAgeMonitor;

    public static void main(String[] args) {
        DataAgeMonitorDriver driver = MainModule.INJECTOR.getInstance(DataAgeMonitorDriver.class);
        driver.execute();
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        redisDataAgeMonitor.monitor();
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        redisDataAgeMonitor.cleanup();
    }

}
