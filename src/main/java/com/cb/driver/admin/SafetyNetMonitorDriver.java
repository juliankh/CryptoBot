package com.cb.driver.admin;

import com.cb.admin.SafetyNetMonitor;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SafetyNetMonitorDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "SafetyNet Monitor";

    @Inject
    private SafetyNetMonitor safetyNetMonitor;

    public static void main(String[] args) {
        SafetyNetMonitorDriver driver = MainModule.INJECTOR.getInstance(SafetyNetMonitorDriver.class);
        driver.execute();
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        safetyNetMonitor.monitor();
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        safetyNetMonitor.cleanup();
    }

}
