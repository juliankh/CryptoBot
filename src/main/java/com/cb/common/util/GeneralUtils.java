package com.cb.common.util;

import org.apache.commons.lang3.StringUtils;

import java.util.NavigableMap;
import java.util.Random;

public class GeneralUtils {

    private static final Random RANDOM = new Random();

    public static int newRandomInt() {
        return Math.abs(RANDOM.nextInt());
    }

    public static <K,V> void pruneNavigableMap(NavigableMap<K,V> map, int limit) {
        while (map.size() > limit) {
            map.pollFirstEntry();
        }
    }

    public static String truncateStringIfNecessary(String s, int length) {
        return truncateStringIfNecessary(s, length, "...");
    }

    public static String truncateStringIfNecessary(String s, int length, String postfix) {
        if (length < 1) {
            throw new RuntimeException("When truncating a string, length provided [" + length + "] < 1");
        }
        if (StringUtils.length(s) <= length) {
            return s;
        }
        return StringUtils.truncate(s, length) + postfix;
    }

}
