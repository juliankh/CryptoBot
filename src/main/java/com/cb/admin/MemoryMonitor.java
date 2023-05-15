package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.common.util.NumberUtils;
import com.cb.db.DbReadOnlyProvider;
import com.cb.db.MiscConfigName;
import com.cb.model.config.MiscConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;
import java.util.Map;

@Slf4j
@Singleton
public class MemoryMonitor {

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private AlertProvider alertProvider;

    public void monitor() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        long inUse = operatingSystemMXBean.getCommittedVirtualMemorySize();
        long total = operatingSystemMXBean.getTotalMemorySize();
        double freeRatio = freeRatio(inUse, total);
        Map<String, MiscConfig> configMap = dbReadOnlyProvider.miscConfig();
        MiscConfig config = configMap.get(MiscConfigName.FREE_MEMORY_THRESHOLD_PERCENT);
        double percentThreshold = config.getValue();
        alertIfAboveLimit(freeRatio, percentThreshold);
    }

    public double freeRatio(long inUse, long total) {
        return (double)(total - inUse) / (double)total;
    }

    public void alertIfAboveLimit(double freeRatio, double percentThreshold) {
        double thresholdRatio = percentThreshold / 100d;
        String precentFreeString = NumberUtils.percentFormat(freeRatio);
        String percentThresholdString = NumberUtils.percentFormat(thresholdRatio);
        if (freeRatio < thresholdRatio) {
            String msg = "Free memory [" + precentFreeString + "] < threshold of [" + percentThresholdString + "]";
            log.warn(msg);
            alertProvider.sendEmailAlert(msg, msg);
        } else {
            log.info("Free memory [" + precentFreeString + "] > threshold of [" + percentThresholdString + "], which is good");
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbReadOnlyProvider.cleanup();
    }

}
