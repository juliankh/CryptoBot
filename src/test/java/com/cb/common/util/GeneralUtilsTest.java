package com.cb.common.util;

import org.junit.Test;

import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class GeneralUtilsTest {

    @Test
    public void pruneNavigableMap_MapBelowLimit() {
        // setup
        NavigableMap<Integer, String> map = new TreeMap<>() {{
            put(1, "one");
            put(2, "two");
            put(3, "three");
        }};

        // engage test
        GeneralUtils.pruneNavigableMap(map, 4);

        // verify
        assertEquals(3, map.size());
    }

    @Test
    public void pruneNavigableMap_MapAtLimit() {
        // setup
        NavigableMap<Integer, String> map = new TreeMap<>() {{
            put(1, "one");
            put(2, "two");
            put(3, "three");
        }};

        // engage test
        GeneralUtils.pruneNavigableMap(map, 3);

        // verify
        assertEquals(3, map.size());
    }

    @Test
    public void pruneNavigableMap_MapOverLimit() {
        // setup
        NavigableMap<Integer, String> map = new TreeMap<>() {{
            put(1, "one");
            put(2, "two");
            put(3, "three");
        }};

        // engage test
        GeneralUtils.pruneNavigableMap(map, 1);

        // verify
        assertEquals(1, map.size());
        NavigableMap<Integer, String> expected = new TreeMap<>() {{
            put(3, "three");
        }};
        assertEquals(expected, map);
    }

}
