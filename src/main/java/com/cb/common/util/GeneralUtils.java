package com.cb.common.util;

import java.util.NavigableMap;

public class GeneralUtils {

    public static <K,V> void pruneNavigableMap(NavigableMap<K,V> map, int limit) {
        while (map.size() > limit) {
            map.pollFirstEntry();
        }
    }

}
