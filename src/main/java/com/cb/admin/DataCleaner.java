package com.cb.admin;

import com.cb.common.util.NumberUtils;
import com.cb.db.DbProvider;
import com.cb.model.config.DataCleanerConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DataCleaner {

    private final DbProvider dbProvider;

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
        log.info("For table [" + table + "] pruned [" + NumberUtils.numberFormat(rowcount) + "] rows which were > [" + hoursLimit + "] hours old");
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
