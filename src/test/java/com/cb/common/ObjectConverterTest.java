package com.cb.common;

import com.cb.common.util.TimeUtils;
import com.cb.model.config.*;
import com.cb.model.config.db.*;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.test.EqualsUtils;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.jdbc.PgArray;

import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.cb.test.CryptoBotTestUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class ObjectConverterTest {

    @Spy
    private CurrencyResolver currencyResolver;

    @InjectMocks
    private ObjectConverter objectConverter;

    @Before
    public void beforeEachTest() {
        Mockito.reset(currencyResolver);
    }

    @Test
    public void matrixOfDoubles() {
        // same
        List<List<Double>> original = Arrays.asList(Arrays.asList(3.4, 5.77),
                Arrays.asList(31.42, -85.2),
                Arrays.asList(5.0, 7.0));
        double[][] expected = {{3.4, 5.77},
                {31.42, -85.2},
                {5, 7}};
        EqualsUtils.assertMatrixEquals(expected, objectConverter.matrixOfDoubles(original));

        // diff values
        List<List<Double>> listsDiffLength1 = Arrays.asList(Arrays.asList(3.4, 5.77),
                Arrays.asList(31.42, -85.2),
                Arrays.asList(5.0, 7.0),
                Arrays.asList(-56.3, 8.9));
        EqualsUtils.assertMatrixNotEquals(expected, objectConverter.matrixOfDoubles(listsDiffLength1));

        // diff values
        List<List<Double>> listsSameSizeButDiffValues = Arrays.asList(Arrays.asList(3.4, 5.77),
                Arrays.asList(31.42, -85.2),
                Arrays.asList(5.0, 7.1));
        EqualsUtils.assertMatrixNotEquals(expected, objectConverter.matrixOfDoubles(listsSameSizeButDiffValues));
    }

    @Test
    public void matrixOfDoubles_exception_emptyOuterListNull() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists that's empty-equivalent: [" + null + "]",
                RuntimeException.class,
                () -> objectConverter.matrixOfDoubles(null));
    }

    @Test
    public void matrixOfDoubles_exception_emptyOuterListEmpty() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists that's empty-equivalent: [" + Collections.emptyList() + "]",
                RuntimeException.class,
                () -> objectConverter.matrixOfDoubles(Collections.emptyList()));
    }

    @Test
    public void matrixOfDoubles_exception_emptyInnerList() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists that contains at least 1 inner List that's empty-equivalent",
                RuntimeException.class,
                () -> objectConverter.matrixOfDoubles(Arrays.asList(Arrays.asList(1.1, 2.2), Collections.emptyList())));
    }

    @Test
    public void matrixOfDoubles_exception_nonUniformLengths() {
        assertThrows(
                "Tried to get matrix of doubles based on List of Lists where the inner Lists are not all of the same size.  The inner Lists are of 3 diff sizes",
                RuntimeException.class,
                () -> objectConverter.matrixOfDoubles(Arrays.asList(Arrays.asList(1.1, 2.2), Arrays.asList(1.1, 2.2, 3.3), Arrays.asList(11.4, 22.5, 33.6, 4.7, 5.8, 6.9))));
    }

    @Test
    public void primitiveArray() {
        assertArrayEquals(ArrayUtils.EMPTY_DOUBLE_ARRAY, objectConverter.primitiveArray(Collections.emptyList()), DOUBLE_COMPARE_DELTA);
        assertArrayEquals(new double[] {45.23}, objectConverter.primitiveArray(Lists.newArrayList(45.23)), DOUBLE_COMPARE_DELTA);
        assertArrayEquals(new double[] {45.23, 2.3, 8.579}, objectConverter.primitiveArray(Lists.newArrayList(45.23, 2.3, 8.579)), DOUBLE_COMPARE_DELTA);
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

        assertArrayEquals(expected, objectConverter.matrix(Lists.newArrayList(ob1, ob2)));
    }

    @Test
    public void convertToDataAgeMonitorConfig() {
        // setup data
        long id = 123;
        String tableName = "tableName1";
        String columnName = "columnName1";
        int minsAgeLimit = 5;
        DbDataAgeMonitorConfig rawConfig = new DbDataAgeMonitorConfig();
        rawConfig.setId(id);
        rawConfig.setTable_name(tableName);
        rawConfig.setColumn_name(columnName);
        rawConfig.setMins_age_limit(minsAgeLimit);

        // engage test
        DataAgeMonitorConfig result = objectConverter.convertToDataAgeMonitorConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(tableName, result.getTableName());
        assertEquals(columnName, result.getColumnName());
        assertEquals(minsAgeLimit, result.getMinsAgeLimit());
    }

    @Test
    public void convertToDataCleanerConfig() {
        // setup data
        long id = 123;
        String tableName = "tableName1";
        String columnName = "columnName1";
        int hoursBack = 10;
        DbDataCleanerConfig rawConfig = new DbDataCleanerConfig();
        rawConfig.setId(id);
        rawConfig.setTable_name(tableName);
        rawConfig.setColumn_name(columnName);
        rawConfig.setHours_back(hoursBack);

        // engage test
        DataCleanerConfig result = objectConverter.convertToDataCleanerConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(tableName, result.getTableName());
        assertEquals(columnName, result.getColumnName());
        assertEquals(hoursBack, result.getHoursBack());
    }

    @Test
    public void convertToQueueMonitorConfig() {
        // setup data
        long id = 456;
        String queueName = "queue1";
        int messageLimit = 100;
        DbQueueMonitorConfig rawConfig = new DbQueueMonitorConfig();
        rawConfig.setId(id);
        rawConfig.setQueue_name(queueName);
        rawConfig.setMessage_limit(messageLimit);

        // engage test
        QueueMonitorConfig result = objectConverter.convertToQueueMonitorConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(queueName, result.getQueueName());
        assertEquals(messageLimit, result.getMessageLimit());
    }

    @Test
    public void convertToKrakenBridgeOrderBookConfig() {
        // setup data
        long id = 567;
        String currencyBase = "BTC";
        String currencyCounter = "USDT";
        int batchSize = 200;
        int secsTimeout = 20;
        DbKrakenBridgeOrderBookConfig rawConfig = new DbKrakenBridgeOrderBookConfig();
        rawConfig.setId(id);
        rawConfig.setCurrency_base(currencyBase);
        rawConfig.setCurrency_counter(currencyCounter);
        rawConfig.setBatch_size(batchSize);
        rawConfig.setSecs_timeout(secsTimeout);

        // engage test
        KrakenBridgeOrderBookConfig result = objectConverter.convertToKrakenBridgeOrderBookConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(CurrencyPair.BTC_USDT, result.getCurrencyPair());
        assertEquals(batchSize, result.getBatchSize());
        assertEquals(secsTimeout, result.getSecsTimeout());
    }

    @Test
    public void convertToMiscConfig() {
        // setup data
        long id = 6789;
        String name = "name1";
        double value = 56.23;
        DbMiscConfig rawConfig = new DbMiscConfig();
        rawConfig.setId(id);
        rawConfig.setName(name);
        rawConfig.setValue(value);

        // engage test
        MiscConfig result = objectConverter.convertToMiscConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(name, result.getName());
        assertEquals(value, result.getValue(), DOUBLE_COMPARE_DELTA);
    }

}
