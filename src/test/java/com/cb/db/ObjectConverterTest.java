package com.cb.db;

import com.cb.model.orderbook.DbKrakenOrderbook;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.postgresql.jdbc.PgArray;

import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;

import static org.junit.Assert.assertArrayEquals;

public class ObjectConverterTest {

    private static final ObjectConverter OBJECT_CONVERTER = new ObjectConverter();

    @Test
    public void matrix() throws SQLException {
        String process1 = "Process1";
        String process2 = "Process2";
        Timestamp exchange_datetime1 = new Timestamp(System.currentTimeMillis());
        Timestamp exchange_datetime2 = new Timestamp(System.currentTimeMillis() + 1);
        java.sql.Date exchange_date1 = new Date(System.currentTimeMillis() + 5);
        java.sql.Date exchange_date2 = new Date(System.currentTimeMillis() + 6);
        double highest_bid_price1 = 1.23;
        double highest_bid_price2 = 1.24;
        double highest_bid_volume1 = 2.34;
        double highest_bid_volume2 = 2.35;
        double lowest_ask_price1 = 3.45;
        double lowest_ask_price2 = 3.46;
        double lowest_ask_volume1 = 4.56;
        double lowest_ask_volume2 = 4.57;
        int bids_hash1 = 9348567;
        int bids_hash2 = 9348568;
        int asks_hash1 = 89073456;
        int asks_hash2 = 89073457;
        Array bids1 = new PgArray(null, 5, "field1");
        Array bids2 = new PgArray(null, 4, "field1");
        Array asks1 = new PgArray(null, 6, "field2");
        Array asks2 = new PgArray(null, 7, "field2");

        DbKrakenOrderbook ob1 = new DbKrakenOrderbook();
        ob1.setProcess(process1);
        ob1.setExchange_datetime(exchange_datetime1);
        ob1.setExchange_date(exchange_date1);
        ob1.setHighest_bid_price(highest_bid_price1);
        ob1.setHighest_bid_volume(highest_bid_volume1);
        ob1.setLowest_ask_price(lowest_ask_price1);
        ob1.setLowest_ask_volume(lowest_ask_volume1);
        ob1.setBids_hash(bids_hash1);
        ob1.setAsks_hash(asks_hash1);
        ob1.setBids(bids1);
        ob1.setAsks(asks1);

        DbKrakenOrderbook ob2 = new DbKrakenOrderbook();
        ob2.setProcess(process2);
        ob2.setExchange_datetime(exchange_datetime2);
        ob2.setExchange_date(exchange_date2);
        ob2.setHighest_bid_price(highest_bid_price2);
        ob2.setHighest_bid_volume(highest_bid_volume2);
        ob2.setLowest_ask_price(lowest_ask_price2);
        ob2.setLowest_ask_volume(lowest_ask_volume2);
        ob2.setBids_hash(bids_hash2);
        ob2.setAsks_hash(asks_hash2);
        ob2.setBids(bids2);
        ob2.setAsks(asks2);

        Object[][] expected = new Object[][] {
                {process1, exchange_datetime1, exchange_date1, highest_bid_price1, highest_bid_volume1, lowest_ask_price1, lowest_ask_volume1, bids_hash1, asks_hash1, bids1, asks1},
                {process2, exchange_datetime2, exchange_date2, highest_bid_price2, highest_bid_volume2, lowest_ask_price2, lowest_ask_volume2, bids_hash2, asks_hash2, bids2, asks2}
        };

        assertArrayEquals(expected, OBJECT_CONVERTER.matrix(Lists.newArrayList(ob1, ob2)));
    }

}
