package com.cb.db;

import com.cb.common.util.NumberUtils;
import com.cb.db.kraken.KrakenTableNameResolver;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.List;

@Slf4j
public class DataCleaner {

    private final KrakenTableNameResolver krakenTableNameResolver;
    private final DbProvider dbProvider;

    public DataCleaner(KrakenTableNameResolver krakenTableNameResolver, DbProvider dbProvider) {
        this.krakenTableNameResolver = krakenTableNameResolver;
        this.dbProvider = dbProvider;
    }

    public void prune() {
        List<Triple<String, String, Integer>> tableCleanupConfig = tableCleanupConfig();
        tableCleanupConfig.parallelStream().forEach(config -> {
            String table = config.getLeft();
            String column = config.getMiddle();
            int daysLimit = config.getRight();
            pruneTable(table, column, daysLimit);
        });
    }

    public void pruneTable(String table, String column, int daysLimit) {
        int rowcount = dbProvider.prune(table, column, daysLimit);
        log.info("For table [" + table + "] pruned [" + NumberUtils.NUMBER_FORMAT.format(rowcount) + "] rows which were > [" + daysLimit + "] days old");
    }

    // Triple: table - column - age in days beyond which to delete
    private List<Triple<String, String, Integer>> tableCleanupConfig() {
        return Lists.newArrayList(
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(CurrencyPair.BTC_USDT), "exchange_datetime", 12)
        );
    }

    public void cleanup() {
        dbProvider.cleanup();
    }

}
