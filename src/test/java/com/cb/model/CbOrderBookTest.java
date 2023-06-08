package com.cb.model;

import com.google.common.collect.Maps;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Test;

import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    @Test
    public void getSpread_EmptyBidsAndOrAsks() {
        assertNull(new CbOrderBook().setBids(null).setAsks(null).getSpread());

        assertNull(new CbOrderBook().setBids(null).setAsks(new TreeMap<>(){{put(1.1, 11.1);}}).getSpread());
        assertNull(new CbOrderBook().setBids(Maps.newTreeMap()).setAsks(new TreeMap<>(){{put(1.1, 11.1);}}).getSpread());

        assertNull(new CbOrderBook().setBids(new TreeMap<>(){{put(1.1, 11.1);}}).setAsks(null).getSpread());
        assertNull(new CbOrderBook().setBids(new TreeMap<>(){{put(1.1, 11.1);}}).setAsks(Maps.newTreeMap()).getSpread());
    }

}
