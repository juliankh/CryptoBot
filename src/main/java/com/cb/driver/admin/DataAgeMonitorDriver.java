package com.cb.driver.admin;

import com.cb.alert.AlertProvider;
import com.cb.common.CurrencyResolver;
import com.cb.db.DataAgeMonitor;
import com.cb.db.DbProvider;
import com.cb.db.kraken.KrakenTableNameResolver;
import com.cb.driver.AbstractDriver;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DataAgeMonitorDriver extends AbstractDriver  {

    private static final String DRIVER_NAME = "Data Age Monitor";

    private final DataAgeMonitor dataAgeMonitor;

    public static void main(String[] args) {
        CurrencyResolver currencyResolver = new CurrencyResolver();
        KrakenTableNameResolver krakenTableNameResolver = new KrakenTableNameResolver(currencyResolver);
        DbProvider dbProvider = new DbProvider();
        AlertProvider alertProvider = new AlertProvider();
        DataAgeMonitor dataAgeMonitor = new DataAgeMonitor(krakenTableNameResolver, dbProvider, alertProvider);
        (new DataAgeMonitorDriver(dataAgeMonitor, alertProvider)).execute();
    }

    public DataAgeMonitorDriver(DataAgeMonitor dataAgeMonitor, AlertProvider alertProvider) {
        super(alertProvider);
        this.dataAgeMonitor = dataAgeMonitor;
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
