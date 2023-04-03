package com.cb.model.orderbook;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Array;
import java.sql.Timestamp;

@Getter
@Setter
@ToString
@EqualsAndHashCode
public class DbKrakenOrderbook {

    private Long id; // db field
    private String process;
    private Timestamp exchange_datetime;
    private java.sql.Date exchange_date;
    private int bids_hash;
    private int asks_hash;
    private Array bids;
    private Array asks;
    private Timestamp created; // db field

}
