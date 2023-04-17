package com.cb.model;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

import java.util.TreeMap;

import static org.junit.Assert.assertEquals;

public class CbOrderBookTest {

    @Test
    public void getSpread() {
        TreeMap<Double, Double> bids = new TreeMap<>();
        bids.put(3.9, 36.5);
        bids.put(5.1, 123.0); // highest bid
        bids.put(2.6, 56.89);

        TreeMap<Double, Double> asks = new TreeMap<>();
        asks.put(13.9, 456.25);
        asks.put(15.1, 3.8567);
        asks.put(12.6, 487.1); // lowest ask

        CbOrderBook orderBook = new CbOrderBook().setBids(bids).setAsks(asks);

        Spread expected = new Spread(Pair.of(5.1, 123.0), Pair.of(12.6, 487.1));
        assertEquals(expected, orderBook.getSpread());
    }

}
