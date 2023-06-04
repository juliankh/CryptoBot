package com.cb.common.util;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class NumberUtilsTest {

    @Test
    public void equals() {
        assertTrue(NumberUtils.equals(0.0, 0.0));
        assertTrue(NumberUtils.equals(0.0, 0.00_000_000_09));
        assertFalse(NumberUtils.equals(0.0, 0.00_000_001));
    }

}
