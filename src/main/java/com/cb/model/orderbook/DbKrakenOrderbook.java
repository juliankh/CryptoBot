package com.cb.model.orderbook;

import lombok.*;

import java.sql.Array;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class DbKrakenOrderbook {

    private Long id; // db field
    private String process;
    private Timestamp exchange_datetime;
    private java.sql.Date exchange_date;
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
