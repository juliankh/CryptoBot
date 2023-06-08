package com.cb.common.util;



import org.junit.jupiter.api.Test;

import java.util.NavigableMap;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.*;

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

    @Test
    public void truncateStringIfNecessary() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> GeneralUtils.truncateStringIfNecessary("some string", 0));
        assertEquals("When truncating a string, length provided [0] < 1", exception.getMessage());

        assertNull(GeneralUtils.truncateStringIfNecessary(null, 1));
        assertEquals("", GeneralUtils.truncateStringIfNecessary("", 5));
        assertEquals("12345", GeneralUtils.truncateStringIfNecessary("12345", 5));
        assertEquals("1234...", GeneralUtils.truncateStringIfNecessary("12345", 4));
        assertEquals("123...", GeneralUtils.truncateStringIfNecessary("12345", 3));
    }

}
