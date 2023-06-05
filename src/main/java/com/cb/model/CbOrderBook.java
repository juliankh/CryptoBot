package com.cb.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.io.Serializable;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode
@ToString
public class CbOrderBook implements Serializable {

    private boolean snapshot;
    private Instant exchangeDatetime;
    private LocalDate exchangeDate;
    private long receivedMicros;
    private TreeMap<Double, Double> bids;
    private TreeMap<Double, Double> asks;

    // lazy loaded
    private Spread spread;

    public synchronized Spread getSpread() {
        if (spread == null && MapUtils.isNotEmpty(bids) && MapUtils.isNotEmpty(asks)) {
            Map.Entry<Double, Double> highestBid = bids.lastEntry();
            Map.Entry<Double, Double> lowestAsk = asks.firstEntry();
            spread = new Spread(Pair.of(highestBid.getKey(), highestBid.getValue()), Pair.of(lowestAsk.getKey(), lowestAsk.getValue()));
        }
        return spread;
    }

}
