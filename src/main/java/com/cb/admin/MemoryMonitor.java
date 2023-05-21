package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.common.util.NumberUtils;
import com.cb.db.DbReadOnlyProvider;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.sun.management.OperatingSystemMXBean;
import lombok.extern.slf4j.Slf4j;

import java.lang.management.ManagementFactory;

@Slf4j
@Singleton
public class MemoryMonitor {

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private AlertProvider alertProvider;

    public void monitor() {
        OperatingSystemMXBean operatingSystemMXBean = (OperatingSystemMXBean)ManagementFactory.getOperatingSystemMXBean();
        long totalMemorySize = operatingSystemMXBean.getTotalMemorySize();
        long committedVirtualMemorySize = operatingSystemMXBean.getCommittedVirtualMemorySize();
        long freeMemorySize = operatingSystemMXBean.getFreeMemorySize();
        long totalSwapSpaceSize = operatingSystemMXBean.getTotalSwapSpaceSize();
        long freeSwapSpaceSize = operatingSystemMXBean.getFreeSwapSpaceSize();

        String committedVirtualOfTotalMemory = percent(committedVirtualMemorySize, totalMemorySize);
        String freeOfTotalMemory = percent(freeMemorySize, totalMemorySize);
        String freeOfTotalSwap = percent(freeSwapSpaceSize, totalSwapSpaceSize);

        log.info("Total Memory Size [" + totalMemorySize + "], Committed Virtual Memory Size [" + committedVirtualMemorySize + "] (" + committedVirtualOfTotalMemory + " of Total), Free Memory Size [" + freeMemorySize + "] (" + freeOfTotalMemory + " of Total), Total Swap Space Size [" + totalSwapSpaceSize + "], Free Swap Space Size [" + freeSwapSpaceSize + "] (" + freeOfTotalSwap + " of Total Swap)");

        /*
        // TODO: determine under what conditions to alert
        Map<String, MiscConfig> configMap = dbReadOnlyProvider.miscConfig();
        MiscConfig config = configMap.get(MiscConfigName.FREE_MEMORY_THRESHOLD_PERCENT);
        double percentThreshold = config.getValue();
        alertIfAboveLimit(freeRatio, percentThreshold);
         */
    }

    public String percent(long numerator, long denominator) {
        return NumberUtils.percentFormat(ratio(numerator, denominator));
    }

    public double ratio(long numerator, long denominator) {
        return (double)(denominator - numerator) / (double)denominator;
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
