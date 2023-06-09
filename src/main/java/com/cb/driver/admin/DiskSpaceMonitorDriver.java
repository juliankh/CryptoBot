package com.cb.driver.admin;

import com.cb.admin.DiskSpaceMonitor;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;

@Slf4j
public class DiskSpaceMonitorDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Disk Space Monitor";

    @Inject
    private DiskSpaceMonitor diskSpaceMonitor;

    public static void main(String[] args) {
        DiskSpaceMonitorDriver driver = MainModule.INJECTOR.getInstance(DiskSpaceMonitorDriver.class);
        driver.execute();
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        diskSpaceMonitor.monitor();
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        diskSpaceMonitor.cleanup();
    }

}
