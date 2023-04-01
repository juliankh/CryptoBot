package com.cb.model.orderbook;

import java.time.Instant;
import java.time.LocalDate;
import java.util.TreeMap;

public class CbOrderBook {

    private Long id; // db field
    private Instant exchange_datetime;
    private LocalDate exchange_date;
    private TreeMap<Double, Double> bids;
    private TreeMap<Double, Double> asks;
    private Instant created; // db field

}
