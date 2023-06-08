package com.cb.model;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import static com.cb.common.util.NumberUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class SpreadTest {

    @Test
    public void test() {
        Spread spread = new Spread(Pair.of(5.1, 123.0), Pair.of(12.6, 487.1));
        assertEquals(5.1, spread.bidPrice(), DOUBLE_COMPARE_DELTA);
        assertEquals(123.0, spread.bidVolume(), DOUBLE_COMPARE_DELTA);
        assertEquals(12.6, spread.askPrice(), DOUBLE_COMPARE_DELTA);
        assertEquals(487.1, spread.askVolume(), DOUBLE_COMPARE_DELTA);
    }

}
