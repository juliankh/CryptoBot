package com.cb.driver.admin;

import com.cb.admin.DiskSpaceMonitor;
import com.cb.driver.AbstractDriver;
import com.cb.module.CryptoBotModule;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiskSpaceMonitorDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Disk Space Monitor";

    @Inject
    private DiskSpaceMonitor diskSpaceMonitor;

    public static void main(String[] args) {
        DiskSpaceMonitorDriver driver = CryptoBotModule.INJECTOR.getInstance(DiskSpaceMonitorDriver.class);
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
