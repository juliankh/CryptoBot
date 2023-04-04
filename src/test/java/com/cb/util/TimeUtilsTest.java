package com.cb.util;

import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.Assert.assertEquals;

public class TimeUtilsTest {

    @Test
    public void ratePerSecond() {
        Instant start = Instant.now();
        assertEquals(52L, TimeUtils.ratePerSecond(start, start.plus(5_725, ChronoUnit.MILLIS), 300L));
        assertEquals(414L, TimeUtils.ratePerSecond(start, start.plus(725, ChronoUnit.MILLIS), 300L));
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

}
