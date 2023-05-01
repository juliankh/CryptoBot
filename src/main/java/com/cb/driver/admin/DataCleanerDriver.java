package com.cb.driver.admin;

import com.cb.admin.DataCleaner;
import com.cb.alert.AlertProvider;
import com.cb.db.DbProvider;
import com.cb.driver.AbstractDriver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataCleanerDriver extends AbstractDriver  {

    private static final String DRIVER_NAME = "Data Cleaner";

    private final DataCleaner dataCleaner;

    public static void main(String[] args) {
        DbProvider dbProvider = new DbProvider();
        AlertProvider alertProvider = new AlertProvider();
        DataCleaner dataCleaner = new DataCleaner(dbProvider);
        (new DataCleanerDriver(dataCleaner, alertProvider)).execute();
    }

    public DataCleanerDriver(DataCleaner dataCleaner, AlertProvider alertProvider) {
        super(alertProvider);
        this.dataCleaner = dataCleaner;
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
