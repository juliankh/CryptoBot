package com.cb.db;

import com.cb.alert.AlertProvider;
import com.cb.db.kraken.KrakenTableNameResolver;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
public class DataAgeMonitor {

    private final KrakenTableNameResolver krakenTableNameResolver;
    private final DbProvider dbProvider;
    private final AlertProvider alertProvider;

    public DataAgeMonitor(KrakenTableNameResolver krakenTableNameResolver, DbProvider dbProvider, AlertProvider alertProvider) {
        this.krakenTableNameResolver = krakenTableNameResolver;
        this.dbProvider = dbProvider;
        this.alertProvider = alertProvider;
    }

    public void monitor() {
        List<Triple<String, String, Integer>> tableMonitorConfig = tableMonitorConfig();
        tableMonitorConfig.parallelStream().forEach(config -> {
            String table = config.getLeft();
            String column = config.getMiddle();
            int ageLimit = config.getRight();
            Instant timeOfLastItem = dbProvider.timeOfLastItem(table, column);
            Instant timeToCompare = Instant.now();
            monitorTable(table, timeOfLastItem, timeToCompare, ageLimit);
        });
    }

    public void monitorTable(String table, Instant timeOfLastItem, Instant timeToCompare, int ageLimit) {
        long minsAge = ChronoUnit.MINUTES.between(timeOfLastItem, timeToCompare);
        if (minsAge > ageLimit) {
            String msg = "For table [" + table + "] the last item is [" + minsAge + "] mins old, which is > limit of [" + ageLimit + "] mins";
            log.warn(msg);
            alertProvider.sendEmailAlert(msg, msg);
        } else {
            log.info("For table [" + table + "] the last item is [" + minsAge + "] mins old, which is within limit of [" + ageLimit + "] mins");
        }
    }

    // Triple: table - column - age in mins beyond which to alert
    private List<Triple<String, String, Integer>> tableMonitorConfig() {
        return Lists.newArrayList(
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(CurrencyPair.BTC_USDT), "exchange_datetime", 5),
            Triple.of("cb.jms_destination_stats", "measured", 5)
        );
    }

    public void cleanup() {
        dbProvider.cleanup();
    }

}
