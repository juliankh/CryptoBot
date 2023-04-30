package com.cb.common.util;

import org.junit.Test;

import java.time.Instant;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static com.cb.test.CryptoBotTestUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {

    @Test
    public void ratePerSecond() {
        Instant start = Instant.now();
        assertEquals(0.45, TimeUtils.ratePerSecond(start, start.plus(100_000, ChronoUnit.MILLIS), 45L), DOUBLE_COMPARE_DELTA);
        assertEquals(45.0, TimeUtils.ratePerSecond(start, start.plus(1_000, ChronoUnit.MILLIS), 45L), DOUBLE_COMPARE_DELTA);
        assertEquals(45_000.0, TimeUtils.ratePerSecond(start, start.plus(0, ChronoUnit.MILLIS), 45L), DOUBLE_COMPARE_DELTA);
    }

    @Test
    public void durationMessage_Millis() {
        assertEquals("0 days 0:00:00.543", TimeUtils.durationMessage(543));
        assertEquals("0 days 0:00:06.543", TimeUtils.durationMessage(6 * TimeUtils.SECOND + 543));
        assertEquals("0 days 0:15:06.543", TimeUtils.durationMessage(15 * TimeUtils.MINUTE + 6 * TimeUtils.SECOND + 543));
        assertEquals("0 days 18:15:06.543", TimeUtils.durationMessage(18 * TimeUtils.HOUR + 15 * TimeUtils.MINUTE + 6 * TimeUtils.SECOND + 543));
        assertEquals("2 days 18:15:06.543", TimeUtils.durationMessage(2 * TimeUtils.DAY + 18 * TimeUtils.HOUR + 15 * TimeUtils.MINUTE + 6 * TimeUtils.SECOND + 543));
        assertEquals("1623 days 18:15:06.543", TimeUtils.durationMessage(1623 * TimeUtils.DAY + 18 * TimeUtils.HOUR + 15 * TimeUtils.MINUTE + 6 * TimeUtils.SECOND + 543));
    }

    @Test
    public void durationMessage_StartEnd() {
        Instant start = Instant.now();
        Instant end = start.plus(15 * TimeUtils.MINUTE + 6 * TimeUtils.SECOND + 543, ChronoUnit.MILLIS);
        assertEquals("0 days 0:15:06.543", TimeUtils.durationMessage(start, end));
    }

    @Test
    public void instant() {
        int year = 1995;
        Month month = Month.APRIL;
        int dayOfMonth = 25;
        int hour = 8;
        int minute = 30;
        int seconds = 45;
        ZoneId zoneId = ZoneOffset.systemDefault();

        Instant result = TimeUtils.instant(year, month, dayOfMonth, hour, minute, seconds, zoneId);

        assertEquals(year, result.atZone(zoneId).getYear());
        assertEquals(month, result.atZone(zoneId).getMonth());
        assertEquals(dayOfMonth, result.atZone(zoneId).getDayOfMonth());
        assertEquals(hour, result.atZone(zoneId).getHour());
        assertEquals(minute, result.atZone(zoneId).getMinute());
        assertEquals(seconds, result.atZone(zoneId).getSecond());
        assertEquals(zoneId, result.atZone(zoneId).getZone());
    }

}
