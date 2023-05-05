package com.cb.driver.admin;

import com.cb.admin.DataCleaner;
import com.cb.driver.AbstractDriver;
import com.cb.injection.module.MainModule;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataCleanerDriver extends AbstractDriver {

    private static final String DRIVER_NAME = "Data Cleaner";

    @Inject
    private DataCleaner dataCleaner;

    public static void main(String[] args) {
        DataCleanerDriver driver = MainModule.INJECTOR.getInstance(DataCleanerDriver.class);
        driver.execute();
    }

    @Override
    protected String getDriverName() {
        return DRIVER_NAME;
    }

    @Override
    protected void executeCustom() {
        dataCleaner.prune();
    }

    @Override
    protected void cleanup() {
        log.info("Cleaning up");
        dataCleaner.cleanup();
    }

}
