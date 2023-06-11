package com.cb.model;

import com.cb.model.json.adapter.InstantToLongConverter;
import com.cb.model.json.adapter.LongToInstantConverter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;

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

    private CurrencyPair currencyPair;
    private boolean snapshot;

    @JsonDeserialize(converter = LongToInstantConverter.class)
    @JsonSerialize(converter = InstantToLongConverter.class)
    private Instant exchangeDatetime;

    private LocalDate exchangeDate;
    private long receivedMicros;
    private TreeMap<Double, Double> bids;
    private TreeMap<Double, Double> asks;
    private long checksum;

    public synchronized Spread spread() {
        if (MapUtils.isNotEmpty(bids) && MapUtils.isNotEmpty(asks)) {
            Map.Entry<Double, Double> highestBid = bids.lastEntry();
            Map.Entry<Double, Double> lowestAsk = asks.firstEntry();
            return new Spread(Pair.of(highestBid.getKey(), highestBid.getValue()), Pair.of(lowestAsk.getKey(), lowestAsk.getValue()));
        }
        return null;
    }

}
