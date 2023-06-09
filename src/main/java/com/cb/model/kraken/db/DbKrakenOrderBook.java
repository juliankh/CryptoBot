package com.cb.model.kraken.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.sql.Array;
import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DbKrakenOrderBook implements Serializable {

    private long id; // db field
    private String process;
    private Timestamp exchange_datetime;
    private java.sql.Date exchange_date;
    private long received_micros;
    private Timestamp created; // db field
    private double highest_bid_price;
    private double highest_bid_volume;
    private double lowest_ask_price;
    private double lowest_ask_volume;
    private int bids_hash;
    private int asks_hash;
    private Array bids;
    private Array asks;

}
