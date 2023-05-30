package com.cb.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.tuple.Pair;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
public class CbOrderBook {

    private Instant exchangeDatetime;
    private LocalDate exchangeDate;
    private long receivedMicros;
    private TreeMap<Double, Double> bids;
    private TreeMap<Double, Double> asks;

    // lazy loaded
    private Spread spread;

    public synchronized Spread getSpread() {
        if (spread == null) {
            Map.Entry<Double, Double> highestBid = bids.lastEntry();
            Map.Entry<Double, Double> lowestAsk = asks.firstEntry();
            spread = new Spread(Pair.of(highestBid.getKey(), highestBid.getValue()), Pair.of(lowestAsk.getKey(), lowestAsk.getValue()));
        }
        return spread;
    }

}
