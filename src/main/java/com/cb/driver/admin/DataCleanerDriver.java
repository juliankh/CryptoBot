package com.cb.driver.admin;

import com.cb.alert.AlertProvider;
import com.cb.common.CurrencyResolver;
import com.cb.db.DataCleaner;
import com.cb.db.DbProvider;
import com.cb.db.kraken.KrakenTableNameResolver;
import com.cb.driver.AbstractDriver;

public class DataCleanerDriver extends AbstractDriver  {

    private static final String DRIVER_NAME = "Data Cleaner";

    private final DataCleaner dataCleaner;

    public static void main(String[] args) {
        CurrencyResolver currencyResolver = new CurrencyResolver();
        KrakenTableNameResolver krakenTableNameResolver = new KrakenTableNameResolver(currencyResolver);
        DbProvider dbProvider = new DbProvider();
        AlertProvider alertProvider = new AlertProvider();
        DataCleaner dataCleaner = new DataCleaner(krakenTableNameResolver, dbProvider);
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
        dataCleaner.cleanup();
    }

}
