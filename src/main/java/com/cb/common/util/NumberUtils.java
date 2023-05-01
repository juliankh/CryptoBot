package com.cb.common.util;

import lombok.extern.slf4j.Slf4j;

import java.text.NumberFormat;

@Slf4j
public class NumberUtils {

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance();
    private static final NumberFormat PERCENT_FORMAT = NumberFormat.getPercentInstance();

    public static synchronized String numberFormat(double d) {
        return NUMBER_FORMAT.format(d);
    }

    public static synchronized String numberFormat(int i) {
        return NUMBER_FORMAT.format(i);
    }

    public static synchronized String percentFormat(double d) {
        return PERCENT_FORMAT.format(d);
    }

}
