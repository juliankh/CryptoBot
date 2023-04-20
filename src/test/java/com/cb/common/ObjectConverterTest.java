package com.cb.common;

import com.cb.common.util.TimeUtils;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.test.EqualsUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Test;
import org.postgresql.jdbc.PgArray;

import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cb.test.CryptoBotTestUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertThrows;

public class ObjectConverterTest {

    private static final ObjectConverter OBJECT_CONVERTER = new ObjectConverter();

    @Test
    public void matrixOfDoubles() {
        // same
        List<List<Double>> original = Arrays.asList(Arrays.asList(3.4, 5.77),
                Arrays.asList(31.42, -85.2),
                Arrays.asList(5.0, 7.0));
        double[][] expected = {{3.4, 5.77},
                {31.42, -85.2},
                {5, 7}};
        EqualsUtils.assertMatrixEquals(expected, OBJECT_CONVERTER.matrixOfDoubles(original));

        // diff values
        List<List<Double>> listsDiffLength1 = Arrays.asList(Arrays.asList(3.4, 5.77),
                Arrays.asList(31.42, -85.2),
                Arrays.asList(5.0, 7.0),
                Arrays.asList(-56.3, 8.9));
        EqualsUtils.assertMatrixNotEquals(expected, OBJECT_CONVERTER.matrixOfDoubles(listsDiffLength1));

        // diff values
        List<List<Double>> listsSameSizeButDiffValues = Arrays.asList(Arrays.asList(3.4, 5.77),
                Arrays.asList(31.42, -85.2),
                Arrays.asList(5.0, 7.1));
        EqualsUtils.assertMatrixNotEquals(expected, OBJECT_CONVERTER.matrixOfDoubles(listsSameSizeButDiffValues));
    }

    @Test
    public void matrixOfDoubles_exception_emptyOuterListNull() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists that's empty-equivalent: [" + null + "]",
                RuntimeException.class,
                () -> OBJECT_CONVERTER.matrixOfDoubles(null));
    }

    @Test
    public void matrixOfDoubles_exception_emptyOuterListEmpty() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists that's empty-equivalent: [" + Collections.emptyList() + "]",
                RuntimeException.class,
                () -> OBJECT_CONVERTER.matrixOfDoubles(Collections.emptyList()));
    }

    @Test
    public void matrixOfDoubles_exception_emptyInnerList() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists that contains at least 1 inner List that's empty-equivalent",
                RuntimeException.class,
                () -> OBJECT_CONVERTER.matrixOfDoubles(Arrays.asList(Arrays.asList(1.1, 2.2), Collections.emptyList())));
    }

    @Test
    public void matrixOfDoubles_exception_nonUniformLengths() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists where the inner Lists are not all of the same size.  The inner Lists are of 3 diff sizes",
                RuntimeException.class,
                () -> OBJECT_CONVERTER.matrixOfDoubles(Arrays.asList(Arrays.asList(1.1, 2.2), Arrays.asList(1.1, 2.2, 3.3), Arrays.asList(11.4, 22.5, 33.6, 4.7, 5.8, 6.9))));
    }

    @Test
    public void primitiveArray() {
        assertArrayEquals(ArrayUtils.EMPTY_DOUBLE_ARRAY, OBJECT_CONVERTER.primitiveArray(Collections.emptyList()), DOUBLE_COMPARE_DELTA);
        assertArrayEquals(new double[] {45.23}, OBJECT_CONVERTER.primitiveArray(Lists.newArrayList(45.23)), DOUBLE_COMPARE_DELTA);
        assertArrayEquals(new double[] {45.23, 2.3, 8.579}, OBJECT_CONVERTER.primitiveArray(Lists.newArrayList(45.23, 2.3, 8.579)), DOUBLE_COMPARE_DELTA);
    }


    @Test
    public void matrix() throws SQLException {
        String process1 = "Process1";
        String process2 = "Process2";
        Timestamp exchange_datetime1 = new Timestamp(System.currentTimeMillis());
        Timestamp exchange_datetime2 = new Timestamp(System.currentTimeMillis() + 1);
        java.sql.Date exchange_date1 = new Date(System.currentTimeMillis() + 5);
        java.sql.Date exchange_date2 = new Date(System.currentTimeMillis() + 6);
        long received_nanos1 = TimeUtils.currentNanos();
        long received_nanos2 = TimeUtils.currentNanos();
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

        DbKrakenOrderBook ob1 = new DbKrakenOrderBook();
        ob1.setProcess(process1);
        ob1.setExchange_datetime(exchange_datetime1);
        ob1.setExchange_date(exchange_date1);
        ob1.setReceived_nanos(received_nanos1);
        ob1.setHighest_bid_price(highest_bid_price1);
        ob1.setHighest_bid_volume(highest_bid_volume1);
        ob1.setLowest_ask_price(lowest_ask_price1);
        ob1.setLowest_ask_volume(lowest_ask_volume1);
        ob1.setBids_hash(bids_hash1);
        ob1.setAsks_hash(asks_hash1);
        ob1.setBids(bids1);
        ob1.setAsks(asks1);

        DbKrakenOrderBook ob2 = new DbKrakenOrderBook();
        ob2.setProcess(process2);
        ob2.setExchange_datetime(exchange_datetime2);
        ob2.setExchange_date(exchange_date2);
        ob2.setReceived_nanos(received_nanos2);
        ob2.setHighest_bid_price(highest_bid_price2);
        ob2.setHighest_bid_volume(highest_bid_volume2);
        ob2.setLowest_ask_price(lowest_ask_price2);
        ob2.setLowest_ask_volume(lowest_ask_volume2);
        ob2.setBids_hash(bids_hash2);
        ob2.setAsks_hash(asks_hash2);
        ob2.setBids(bids2);
        ob2.setAsks(asks2);

        Object[][] expected = new Object[][] {
                {process1, exchange_datetime1, exchange_date1, received_nanos1, highest_bid_price1, highest_bid_volume1, lowest_ask_price1, lowest_ask_volume1, bids_hash1, asks_hash1, bids1, asks1},
                {process2, exchange_datetime2, exchange_date2, received_nanos2, highest_bid_price2, highest_bid_volume2, lowest_ask_price2, lowest_ask_volume2, bids_hash2, asks_hash2, bids2, asks2}
        };

        assertArrayEquals(expected, OBJECT_CONVERTER.matrix(Lists.newArrayList(ob1, ob2)));
    }

}
