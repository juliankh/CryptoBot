package com.cb.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Slf4j
public final class TimeUtils {

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    public static void sleepQuietlyForSecs(int secs) {
        sleepQuietlyForMillis(secs * SECOND);
    }

    public static void sleepQuietlyForMins(int mins) {
        sleepQuietlyForMillis(mins * MINUTE);
    }

    public static void sleepQuietlyForMillis(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("Got interrupted while trying to sleep for " + millis + " millis.  Logging, but otherwise ignoring.", e);
        }
    }

    public static String durationMessage(Instant start) {
        return durationMessage(start, Instant.now());
    }

    public static String durationMessage(Instant start, Instant end) {
        return durationMessage(end.toEpochMilli() - start.toEpochMilli());
    }

    public static String durationMessage(long millisDuration) {
        return DurationFormatUtils.formatDuration(millisDuration, "d 'days' H:mm:ss.S");
    }

    public static long ratePerSecond(Instant start, Instant end, long quantity) {
        long millisDuration = ChronoUnit.MILLIS.between(start, end);
        double d = (double)quantity / (double)millisDuration * 1000;
        return Math.round(d);
    }

}
