package com.cb.common.util;

import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;

@Slf4j
public class NumberUtils {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();

    public static synchronized String format(double d) {
        return NumberUtils.NUMBER_FORMAT.format(d);
    }

    public static synchronized String format(int i) {
        return NumberUtils.NUMBER_FORMAT.format(i);
    }

}
