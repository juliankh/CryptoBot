package com.cb.common;

import com.cb.common.util.TimeUtils;
import com.cb.model.CbOrderBook;
import com.cb.model.config.*;
import com.cb.model.config.db.*;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBook;
import com.cb.test.EqualsUtils;
import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.postgresql.jdbc.PgArray;

import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static com.cb.test.CryptoBotTestUtils.DOUBLE_COMPARE_DELTA;
import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ObjectConverterTest {

    @Spy
    private CurrencyResolver currencyResolver;

    @Mock
    private Gson gson;

    @InjectMocks
    private ObjectConverter objectConverter;

    @Before
    public void beforeEachTest() {
        reset(currencyResolver);
        reset(gson);
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
        long received_micros1 = TimeUtils.currentMicros();
        long received_micros2 = TimeUtils.currentMicros();
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
        ob1.setReceived_micros(received_micros1);
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
        ob2.setReceived_micros(received_micros2);
        ob2.setHighest_bid_price(highest_bid_price2);
        ob2.setHighest_bid_volume(highest_bid_volume2);
        ob2.setLowest_ask_price(lowest_ask_price2);
        ob2.setLowest_ask_volume(lowest_ask_volume2);
        ob2.setBids_hash(bids_hash2);
        ob2.setAsks_hash(asks_hash2);
        ob2.setBids(bids2);
        ob2.setAsks(asks2);

        Object[][] expected = new Object[][] {
                {process1, exchange_datetime1, exchange_date1, received_micros1, highest_bid_price1, highest_bid_volume1, lowest_ask_price1, lowest_ask_volume1, bids_hash1, asks_hash1, bids1, asks1},
                {process2, exchange_datetime2, exchange_date2, received_micros2, highest_bid_price2, highest_bid_volume2, lowest_ask_price2, lowest_ask_volume2, bids_hash2, asks_hash2, bids2, asks2}
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
    public void convertToRedisDataAgeMonitorConfig() {
        // setup data
        long id = 123;
        String redisKey = "redisKey1";
        int minsAgeLimit = 5;
        DbRedisDataAgeMonitorConfig rawConfig = new DbRedisDataAgeMonitorConfig();
        rawConfig.setId(id);
        rawConfig.setRedis_key(redisKey);
        rawConfig.setMins_age_limit(minsAgeLimit);

        // engage test
        RedisDataAgeMonitorConfig result = objectConverter.convertToRedisDataAgeMonitorConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(redisKey, result.getRedisKey());
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
    public void convertToSafetyNetConfig() {
        // setup data
        long id = 123;
        String processToken = "processToken1";
        String processSubtoken = "processSubtoken1";
        DbSafetyNetConfig rawConfig = new DbSafetyNetConfig();
        rawConfig.setId(id);
        rawConfig.setProcess_token(processToken);
        rawConfig.setProcess_subtoken(processSubtoken);

        // engage test
        SafetyNetConfig result = objectConverter.convertToSafetyNetConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(processToken, result.getProcessToken());
        assertEquals(processSubtoken, result.getProcessSubToken());
    }

    @Test
    public void convertToRedisDataCleanerConfig() {
        // setup data
        long id = 123;
        String redisKey = "redisKey1";
        int minsBack = 10;
        DbRedisDataCleanerConfig rawConfig = new DbRedisDataCleanerConfig();
        rawConfig.setId(id);
        rawConfig.setRedis_key(redisKey);
        rawConfig.setMins_back(minsBack);

        // engage test
        RedisDataCleanerConfig result = objectConverter.convertToRedisDataCleanerConfig(rawConfig);

        // verify results
        assertEquals(id, result.getId());
        assertEquals(redisKey, result.getRedisKey());
        assertEquals(minsBack, result.getMinsBack());
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

    @Test
    public void priceAndQuantity() {
        // setup data
        double price = 10.1;
        double volume = 2.3;
        LimitOrder limitOrder = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(volume), null, null, null, BigDecimal.valueOf(price));

        // engage test
        Pair<Double, Double> result = objectConverter.priceAndQuantity(limitOrder);

        // verify
        assertEquals(price, result.getLeft(), DOUBLE_COMPARE_DELTA);
        assertEquals(volume, result.getRight(), DOUBLE_COMPARE_DELTA);
    }

    @Test
    public void quoteList() {
        // setup data
        double price1 = 10.1;
        double price2 = 10.3;
        double price3 = 10.2;

        double volume1 = 2.3;
        double volume2 = 2.4;
        double volume3 = 2.5;

        LimitOrder limitOrder1 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(volume1), null, null, null, BigDecimal.valueOf(price1));
        LimitOrder limitOrder2 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(volume2), null, null, null, BigDecimal.valueOf(price2));
        LimitOrder limitOrder3 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(volume3), null, null, null, BigDecimal.valueOf(price3));

        // engage test
        List<Pair<Double, Double>> result = objectConverter.quoteList(Lists.newArrayList(limitOrder1, limitOrder2, limitOrder3));

        // verify
        Pair<Double, Double> quote1 = result.get(0);
        Pair<Double, Double> quote2 = result.get(1);
        Pair<Double, Double> quote3 = result.get(2);

        assertEquals(price1, quote1.getLeft(), DOUBLE_COMPARE_DELTA);
        assertEquals(price2, quote2.getLeft(), DOUBLE_COMPARE_DELTA);
        assertEquals(price3, quote3.getLeft(), DOUBLE_COMPARE_DELTA);

        assertEquals(volume1, quote1.getRight(), DOUBLE_COMPARE_DELTA);
        assertEquals(volume2, quote2.getRight(), DOUBLE_COMPARE_DELTA);
        assertEquals(volume3, quote3.getRight(), DOUBLE_COMPARE_DELTA);
    }

    @Test
    public void quoteTreeMap() {
        // setup data
        double price1 = 10.1;
        double price2 = 10.3;
        double price3 = 10.2;

        double volume1 = 2.3;
        double volume2 = 2.4;
        double volume3 = 2.5;

        LimitOrder limitOrder1 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(volume1), null, null, null, BigDecimal.valueOf(price1));
        LimitOrder limitOrder2 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(volume2), null, null, null, BigDecimal.valueOf(price2));
        LimitOrder limitOrder3 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(volume3), null, null, null, BigDecimal.valueOf(price3));

        List<LimitOrder> limitOrders = Lists.newArrayList(limitOrder1, limitOrder2, limitOrder3);

        // engage test
        TreeMap<Double, Double> result = objectConverter.quoteTreeMap(limitOrders);

        // verify results
        List<Map.Entry<Double, Double>> resultList = Lists.newArrayList(result.entrySet());

        assertEquals(limitOrders.size(), resultList.size());

        Map.Entry<Double, Double> resultQuote1 = resultList.get(0);
        Map.Entry<Double, Double> resultQuote2 = resultList.get(1);
        Map.Entry<Double, Double> resultQuote3 = resultList.get(2);

        assertEquals(price1, resultQuote1.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(volume1, resultQuote1.getValue(), DOUBLE_COMPARE_DELTA);

        assertEquals(price3, resultQuote2.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(volume3, resultQuote2.getValue(), DOUBLE_COMPARE_DELTA);

        assertEquals(price2, resultQuote3.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(volume2, resultQuote3.getValue(), DOUBLE_COMPARE_DELTA);
    }

    @Test
    public void convertToCbOrderBook() {
        // setup data
        double bid1Price = 10.1;
        double bid2Price = 10.3;
        double bid3Price = 10.2;

        double bid1Volume = 2.3;
        double bid2Volume = 2.4;
        double bid3Volume = 2.5;

        double ask1Price = 11.77;
        double ask2Price = 11.55;
        double ask3Price = 11.66;

        double ask1Volume = 5.33;
        double ask2Volume = 5.44;
        double ask3Volume = 5.55;

        java.util.Date date = new java.util.Date(System.currentTimeMillis());

        LimitOrder bid1 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(bid1Volume), null, null, null, BigDecimal.valueOf(bid1Price));
        LimitOrder bid2 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(bid2Volume), null, null, null, BigDecimal.valueOf(bid2Price));
        LimitOrder bid3 = new LimitOrder(Order.OrderType.BID, BigDecimal.valueOf(bid3Volume), null, null, null, BigDecimal.valueOf(bid3Price));
        LimitOrder ask1 = new LimitOrder(Order.OrderType.ASK, BigDecimal.valueOf(ask1Volume), null, null, null, BigDecimal.valueOf(ask1Price));
        LimitOrder ask2 = new LimitOrder(Order.OrderType.ASK, BigDecimal.valueOf(ask2Volume), null, null, null, BigDecimal.valueOf(ask2Price));
        LimitOrder ask3 = new LimitOrder(Order.OrderType.ASK, BigDecimal.valueOf(ask3Volume), null, null, null, BigDecimal.valueOf(ask3Price));
        List<LimitOrder> bids = Lists.newArrayList(bid1, bid2, bid3);
        List<LimitOrder> asks = Lists.newArrayList(ask1, ask2, ask3);
        OrderBook orderBook = new OrderBook(date, asks, bids);

        long micros = 121212L;

        KrakenOrderBook krakenOrderBook = new KrakenOrderBook().setProcess("Process1").setMicroSeconds(micros).setOrderBook(orderBook);

        // engage test
        CbOrderBook result = objectConverter.convertToCbOrderBook(krakenOrderBook);

        // verify results
        assertEquals(date.toInstant(), result.getExchangeDatetime());
        assertEquals(LocalDate.ofInstant(orderBook.getTimeStamp().toInstant(), ZoneId.systemDefault()), result.getExchangeDate());
        assertEquals(micros, result.getReceivedNanos());

        List<Map.Entry<Double, Double>> resultBidList = Lists.newArrayList(result.getBids().entrySet());
        List<Map.Entry<Double, Double>> resultAskList = Lists.newArrayList(result.getAsks().entrySet());

        assertEquals(bids.size(), resultBidList.size());
        assertEquals(asks.size(), resultAskList.size());

        Map.Entry<Double, Double> resultBid1 = resultBidList.get(0);
        Map.Entry<Double, Double> resultBid2 = resultBidList.get(1);
        Map.Entry<Double, Double> resultBid3 = resultBidList.get(2);

        assertEquals(bid1Price, resultBid1.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(bid1Volume, resultBid1.getValue(), DOUBLE_COMPARE_DELTA);

        assertEquals(bid3Price, resultBid2.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(bid3Volume, resultBid2.getValue(), DOUBLE_COMPARE_DELTA);

        assertEquals(bid2Price, resultBid3.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(bid2Volume, resultBid3.getValue(), DOUBLE_COMPARE_DELTA);

        Map.Entry<Double, Double> resultAsk1 = resultAskList.get(0);
        Map.Entry<Double, Double> resultAsk2 = resultAskList.get(1);
        Map.Entry<Double, Double> resultAsk3 = resultAskList.get(2);

        assertEquals(ask2Price, resultAsk1.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(ask2Volume, resultAsk1.getValue(), DOUBLE_COMPARE_DELTA);

        assertEquals(ask3Price, resultAsk2.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(ask3Volume, resultAsk2.getValue(), DOUBLE_COMPARE_DELTA);

        assertEquals(ask1Price, resultAsk3.getKey(), DOUBLE_COMPARE_DELTA);
        assertEquals(ask1Volume, resultAsk3.getValue(), DOUBLE_COMPARE_DELTA);
    }

    @Test
    public void convertToRedisPayload() {
        // setup
        long micros1 = 123123;
        long micros2 = 34221;
        String json1 = "SomeJson1";
        String json2 = "SomeJson2";
        CbOrderBook orderBook1 = new CbOrderBook().setReceivedNanos(micros1);
        CbOrderBook orderBook2 = new CbOrderBook().setReceivedNanos(micros2);
        List<CbOrderBook> orderBooks = Lists.newArrayList(orderBook1, orderBook2);
        when(gson.toJson(orderBook1)).thenReturn(json1);
        when(gson.toJson(orderBook2)).thenReturn(json2);

        // engage test
        Map<String, Double> result = objectConverter.convertToRedisPayload(orderBooks);

        // verify
        assertEquals(2, result.size());
        assertEquals(micros1, result.get(json1), DOUBLE_COMPARE_DELTA);
        assertEquals(micros2, result.get(json2), DOUBLE_COMPARE_DELTA);
    }

}
