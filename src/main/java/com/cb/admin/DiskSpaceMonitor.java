package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.common.util.NumberUtils;
import com.cb.db.DbReadOnlyProvider;
import com.cb.db.MiscConfigName;
import com.cb.model.config.MiscConfig;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Map;

@Slf4j
@Singleton
public class DiskSpaceMonitor {

    @Inject
    private DbReadOnlyProvider dbReadOnlyProvider;

    @Inject
    private AlertProvider alertProvider;

    public void monitor() {
        File f = new File("/");
        long totalSpace = f.getTotalSpace();
        long usableSpace = f.getFreeSpace();
        double usableRatio = usableRatio(totalSpace, usableSpace);
        Map<String, MiscConfig> configMap = dbReadOnlyProvider.miscConfig();
        MiscConfig config = configMap.get(MiscConfigName.FREE_DISK_SPACE_THRESHOLD_PERCENT);
        double percentThreshold = config.getValue();
        alertIfAboveLimit(usableRatio, percentThreshold);
    }

    public double usableRatio(long total, long usable) {
        return (double)usable / (double)total;
    }

    public void alertIfAboveLimit(double usableRatio, double percentThreshold) {
        double thresholdRatio = percentThreshold / 100d;
        String precentUsableString = NumberUtils.percentFormat(usableRatio);
        String percentThresholdString = NumberUtils.percentFormat(thresholdRatio);
        if (usableRatio < thresholdRatio) {
            String msg = "Free/usable disk space [" + precentUsableString + "] < threshold of [" + percentThresholdString + "]";
            log.warn(msg);
            alertProvider.sendEmailAlert(msg, msg);
        } else {
            log.info("Free/usable disk space [" + precentUsableString + "] > threshold of [" + percentThresholdString + "], which is good");
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbReadOnlyProvider.cleanup();
    }

}
