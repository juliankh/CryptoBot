package com.cb.admin;

import com.cb.common.util.NumberUtils;
import com.cb.db.ReadOnlyDao;
import com.cb.db.WriteDao;
import com.cb.model.config.DataCleanerConfig;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;
import java.util.stream.Collectors;

@Deprecated
@Slf4j
@Singleton
public class DataCleaner {

    @Inject
    private ReadOnlyDao readOnlyDao;

    @Inject
    private WriteDao writeDao;

    public void prune() {
        List<DataCleanerConfig> configs = readOnlyDao.dataCleanerConfig();
        log.info("Configs:\n\t" + configs.parallelStream().map(Object::toString).sorted().collect(Collectors.joining("\n\t")));
        configs.parallelStream().forEach(config -> {
            String table = config.getTableName();
            String column = config.getColumnName();
            int hoursLimit = config.getHoursBack();
            pruneTable(table, column, hoursLimit);
        });
    }

    public void pruneTable(String table, String column, int hoursLimit) {
        long rowcount = writeDao.prune(table, column, hoursLimit);
        log.info("For table [" + table + "] pruned [" + NumberUtils.numberFormat(rowcount) + "] rows which were > [" + hoursLimit + "] hours old");
    }

    public void cleanup() {
        log.info("Cleaning up");
        readOnlyDao.cleanup();
        writeDao.cleanup();
    }

}
