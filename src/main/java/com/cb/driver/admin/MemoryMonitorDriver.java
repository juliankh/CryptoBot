package com.cb.driver.admin;

import com.cb.admin.MemoryMonitor;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class MemoryMonitorDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Memory Monitor";

    @Inject
    private MemoryMonitor memoryMonitor;

    public static void main(String[] args) {
        MemoryMonitorDriver driver = MainModule.INJECTOR.getInstance(MemoryMonitorDriver.class);
        driver.execute();
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        memoryMonitor.monitor();
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        memoryMonitor.cleanup();
    }

}
