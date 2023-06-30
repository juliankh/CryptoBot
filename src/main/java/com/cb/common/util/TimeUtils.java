package com.cb.common.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;

@Slf4j
public final class TimeUtils {

    public static final long SECOND = 1000;
    public static final long MINUTE = 60 * SECOND;
    public static final long HOUR = 60 * MINUTE;
    public static final long DAY = 24 * HOUR;

    public static long currentMicros() {
        return micros(Instant.now());
    }

    public static long micros(Instant instant) {
        return (instant.getEpochSecond() * 1_000_000_000 + instant.getNano()) / 1000;
    }

    public static void sleepQuietlyForSecs(int secs) {
        sleepQuietlyForMillis(secs * SECOND);
    }

    public static void sleepQuietlyForMins(int mins) {
        sleepQuietlyForMillis(mins * MINUTE);
    }

    public static void sleepQuietlyForever() {
        sleepQuietlyForMins(Integer.MAX_VALUE);
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
        long durationMillis = end.toEpochMilli() - start.toEpochMilli();
        if (durationMillis < 0) {
            throw new RuntimeException("Duration should be non-negative, but is [" + durationMillis + "] when comparing Start Date [" + start + "] and End Date [" + end + "]");
        }
        return durationMessage(durationMillis);
    }

    public static String durationMessage(long millisDuration) {
        return DurationFormatUtils.formatDuration(millisDuration, "d 'days' H:mm:ss.S");
    }

    public static double ratePerSecond(Instant start, Instant end, long quantity) {
        long millisDuration = Math.max(ChronoUnit.MILLIS.between(start, end), 1L);
        return (double)quantity / (double)millisDuration * 1000d;
    }

    public static Instant instant(int year, Month month, int dayOfMonth, int hour, int minute, int second) {
        return instant(year, month, dayOfMonth, hour, minute, second, ZoneOffset.systemDefault());
    }

    public static Instant instant(int year, Month month, int dayOfMonth, int hour, int minute, int second, ZoneId zone) {
        return LocalDateTime.of(year, month, dayOfMonth, hour, minute, second).atZone(zone).toInstant();
    }

    @SneakyThrows
    public static <T> T runTimedCallable_ObjectOutput(Callable<T> callable, String action) {
        Instant start = Instant.now();
        T result = callable.call();
        Instant end = Instant.now();
        log.info(action + " took [" + TimeUtils.durationMessage(start, end) + "]");
        return result;
    }

    @SneakyThrows
    public static long runTimedCallable_NumberedOutput(Callable<Number> callable, String action, String itemType) {
        Instant start = Instant.now();
        Number countRaw = callable.call();
        long count = countRaw.longValue();
        Instant end = Instant.now();
        double queryRate = TimeUtils.ratePerSecond(start, end, count);
        log.info(action + " [" + NumberUtils.numberFormat(count) + "] of [" + itemType + "] took [" + TimeUtils.durationMessage(start, end) + "] at rate of [" + NumberUtils.numberFormat(queryRate) + "/sec]");
        return count;
    }

    @SneakyThrows
    public static <T extends Collection<?>> T runTimedCallable_CollectionOutput(Callable<T> callable, String action, String itemType) {
        Instant start = Instant.now();
        T result = callable.call();
        Instant end = Instant.now();
        double queryRate = TimeUtils.ratePerSecond(start, end, result.size());
        log.info(action + " [" + NumberUtils.numberFormat(result.size()) + "] of [" + itemType + "] took [" + TimeUtils.durationMessage(start, end) + "] at rate of [" + NumberUtils.numberFormat(queryRate) + "/sec]");
        return result;
    }

    @SneakyThrows
    public static <K, V> Map<K, V> runTimedCallable_MapOutput(Callable<Map<K, V>> callable, String action, String itemType) {
        Instant start = Instant.now();
        Map<K, V> result = callable.call();
        Instant end = Instant.now();
        double queryRate = TimeUtils.ratePerSecond(start, end, result.size());
        log.info(action + " [" + NumberUtils.numberFormat(result.size()) + "] of [" + itemType + "] took [" + TimeUtils.durationMessage(start, end) + "] at rate of [" + NumberUtils.numberFormat(queryRate) + "/sec]");
        return result;
    }

    public static void loopForeverAsync(Runnable runnable, int sleepSecs) {
        CompletableFuture.runAsync(() -> {
            loopForever(runnable, sleepSecs);
        });
    }

    public static void loopForever(Runnable runnable, int sleepSecs) {
        while (true) {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("Problem while trying to execute a runnable while looping forever", e);
                throw e;
            }
            TimeUtils.sleepQuietlyForSecs(sleepSecs);
        }
    }

}
