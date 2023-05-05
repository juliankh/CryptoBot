package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbReadOnlyProvider;
import com.cb.model.config.DataAgeMonitorConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class DataAgeMonitor {

    @Inject
    private DbReadOnlyProvider dbProvider;

    @Inject
    private AlertProvider alertProvider;

    public void monitor() {
        List<DataAgeMonitorConfig> configs = dbProvider.retrieveDataAgeMonitorConfig();
        log.info("Configs:\n\t" + configs.parallelStream().map(Object::toString).sorted().collect(Collectors.joining("\n\t")));
        configs.parallelStream().forEach(config -> {
            String table = config.getTableName();
            String column = config.getColumnName();
            int ageLimit = config.getMinsAgeLimit();
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

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
