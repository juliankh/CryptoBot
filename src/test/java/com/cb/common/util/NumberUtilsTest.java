package com.cb.common.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class NumberUtilsTest {

    @Test
    public void equals() {
        assertTrue(NumberUtils.equals(0.0, 0.0));
        assertTrue(NumberUtils.equals(0.0, 0.00_000_000_09));
        assertFalse(NumberUtils.equals(0.0, 0.00_000_001));
    }

}
