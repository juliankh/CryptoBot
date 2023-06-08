package com.cb.model.config;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class MiscConfigTest {

    @Test
    public void intValue() {
        assertEquals(123, new MiscConfig().setValue(123).intValue());
        assertEquals(123, new MiscConfig().setValue(123.4).intValue());
        assertEquals(123, new MiscConfig().setValue(123.6).intValue());
    }

}
