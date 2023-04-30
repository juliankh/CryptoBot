package com.cb.db;

import com.cb.common.util.NumberUtils;
import com.cb.model.config.DataCleanerConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class DataCleaner {

    private final DbProvider dbProvider;

    public DataCleaner(DbProvider dbProvider) {
        this.dbProvider = dbProvider;
    }

    // TODO: check disk space
    public void prune() {
        List<DataCleanerConfig> configs = dbProvider.retrieveDataCleanerConfig();
        log.info("Configs:\n\t" + configs.parallelStream().map(Object::toString).sorted().collect(Collectors.joining("\n\t")));
        configs.parallelStream().forEach(config -> {
            String table = config.getTableName();
            String column = config.getColumnName();
            int hoursLimit = config.getHoursBack();
            pruneTable(table, column, hoursLimit);
        });
    }

    public void pruneTable(String table, String column, int hoursLimit) {
        int rowcount = dbProvider.prune(table, column, hoursLimit);
        log.info("For table [" + table + "] pruned [" + NumberUtils.format(rowcount) + "] rows which were > [" + hoursLimit + "] hours old");
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
