package com.cb.driver.admin;

import com.cb.admin.DiskSpaceMonitor;
import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import com.cb.driver.AbstractDriver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DiskSpaceMonitorDriver extends AbstractDriver  {

    private static final String DRIVER_NAME = "Disk Space Monitor";

    private final DiskSpaceMonitor diskSpaceMonitor;

    public static void main(String[] args) {
        DbProvider dbProvider = new DbProvider();
        AlertProvider alertProvider = new AlertProvider();
        DiskSpaceMonitor diskSpaceMonitor = new DiskSpaceMonitor(dbProvider, alertProvider);
        (new DiskSpaceMonitorDriver(diskSpaceMonitor, alertProvider)).execute();
    }

    public DiskSpaceMonitorDriver(DiskSpaceMonitor diskSpaceMonitor, AlertProvider alertProvider) {
        super(alertProvider);
        this.diskSpaceMonitor = diskSpaceMonitor;
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
