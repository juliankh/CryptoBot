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

}
