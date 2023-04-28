package com.cb.db;

import com.cb.common.util.NumberUtils;
import com.cb.db.kraken.KrakenTableNameResolver;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.knowm.xchange.currency.Currency;
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
            int hoursLimit = config.getRight();
            pruneTable(table, column, hoursLimit);
        });
    }

    public void pruneTable(String table, String column, int hoursLimit) {
        int rowcount = dbProvider.prune(table, column, hoursLimit);
        log.info("For table [" + table + "] pruned [" + NumberUtils.NUMBER_FORMAT.format(rowcount) + "] rows which were > [" + hoursLimit + "] hours old");
    }

    // TODO: reduce the hours to keep to 24
    // Triple: table - column - age in days beyond which to delete
    private List<Triple<String, String, Integer>> tableCleanupConfig() {
        return Lists.newArrayList(
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(CurrencyPair.BTC_USDT),                          "exchange_datetime", 35),
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(new CurrencyPair(Currency.SOL, Currency.USD)),   "exchange_datetime", 35),
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(CurrencyPair.ATOM_USD),                          "exchange_datetime", 35),
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(CurrencyPair.LINK_USD),                          "exchange_datetime", 35),
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(new CurrencyPair(Currency.MXC, Currency.USD)),   "exchange_datetime", 35),
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(new CurrencyPair(Currency.CHR, Currency.USD)),   "exchange_datetime", 35),
            Triple.of(krakenTableNameResolver.krakenOrderBookTable(new CurrencyPair(Currency.MXC, Currency.USD)),   "exchange_datetime", 35)
        );
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
