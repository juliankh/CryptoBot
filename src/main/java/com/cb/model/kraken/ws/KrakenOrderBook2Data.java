package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenOrderBook2Data {

    private String symbol;
    private long checksum;
    private Instant timestamp;
    private List<KrakenOrderBookLevel> bids;
    private List<KrakenOrderBookLevel> asks;

}
