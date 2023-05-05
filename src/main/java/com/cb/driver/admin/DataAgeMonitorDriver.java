package com.cb.driver.admin;

import com.cb.admin.DataAgeMonitor;
import com.cb.driver.AbstractDriver;
import com.cb.module.CryptoBotModule;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataAgeMonitorDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Data Age Monitor";

    @Inject
    private DataAgeMonitor dataAgeMonitor;

    public static void main(String[] args) {
        DataAgeMonitorDriver driver = CryptoBotModule.INJECTOR.getInstance(DataAgeMonitorDriver.class);
        driver.execute();
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        dataAgeMonitor.monitor();
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        dataAgeMonitor.cleanup();
    }

}
