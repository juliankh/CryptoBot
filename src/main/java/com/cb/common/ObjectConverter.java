package com.cb.common;

import com.cb.common.util.TimeUtils;
import com.cb.db.DbUtils;
import com.cb.db.DbWriteProvider;
import com.cb.model.CbOrderBook;
import com.cb.model.config.*;
import com.cb.model.config.db.*;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.jms.XchangeKrakenOrderBook;
import com.cb.model.kraken.ws.KrakenOrderBook2Data;
import com.cb.model.kraken.ws.KrakenOrderBookLevel;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Singleton
public class ObjectConverter {

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private Gson gson;

    public double[] primitiveArray(Collection<Double> collection) {
        return ArrayUtils.toPrimitive(collection.toArray(new Double[0]));
    }

    public double[][] matrixOfDoubles(List<List<Double>> lists) {
        if (CollectionUtils.isEmpty(lists)) {
            throw new RuntimeException("Tried to get matrix of doubles based on List of Lists that's empty-equivalent: [" + lists + "]");
        }
        if (lists.stream().map(CollectionUtils::isEmpty).collect(Collectors.toSet()).contains(true)) {
            throw new RuntimeException("Tried to get matrix of doubles based on List of Lists that contains at least 1 inner List that's empty-equivalent");
        }
        long numUniqueInnerSizes = lists.stream().map(Collection::size).toList().stream().distinct().count();
        if (numUniqueInnerSizes > 1) {
            throw new RuntimeException("Tried to get matrix of doubles based on List of Lists where the inner Lists are not all of the same size.  The inner Lists are of " + numUniqueInnerSizes + " diff sizes");
        }
        double[][] matrix = new double[lists.size()][lists.get(0).size()];
        for (int i = 0; i < lists.size(); ++i) {
            List<Double> list = lists.get(i);
            for (int j = 0; j < list.size(); ++j) {
                Double value = list.get(j);
                matrix[i][j] = value;
            }
        }
        return matrix;
    }

    public DataAgeMonitorConfig convertToDataAgeMonitorConfig(DbDataAgeMonitorConfig raw) {
        return new DataAgeMonitorConfig()
                .setId(raw.getId())
                .setTableName(raw.getTable_name())
                .setColumnName(raw.getColumn_name())
                .setMinsAgeLimit(raw.getMins_age_limit());
    }

    public RedisDataAgeMonitorConfig convertToRedisDataAgeMonitorConfig(DbRedisDataAgeMonitorConfig raw) {
        return new RedisDataAgeMonitorConfig()
                .setId(raw.getId())
                .setRedisKey(raw.getRedis_key())
                .setMinsAgeLimit(raw.getMins_age_limit());
    }

    public DataCleanerConfig convertToDataCleanerConfig(DbDataCleanerConfig raw) {
        return new DataCleanerConfig()
                .setId(raw.getId())
                .setTableName(raw.getTable_name())
                .setColumnName(raw.getColumn_name())
                .setHoursBack(raw.getHours_back());
    }

    public RedisDataCleanerConfig convertToRedisDataCleanerConfig(DbRedisDataCleanerConfig raw) {
        return new RedisDataCleanerConfig()
                .setId(raw.getId())
                .setRedisKey(raw.getRedis_key())
                .setMinsBack(raw.getMins_back());
    }

    public ProcessConfig convertToProcessConfig(DbProcessConfig raw) {
        return new ProcessConfig()
                .setId(raw.getId())
                .setProcessToken(raw.getProcess_token())
                .setProcessSubToken(raw.getProcess_subtoken())
                .setActive(raw.isActive());
    }

    public QueueMonitorConfig convertToQueueMonitorConfig(DbQueueMonitorConfig raw) {
        return new QueueMonitorConfig()
                .setId(raw.getId())
                .setQueueName(raw.getQueue_name())
                .setMessageLimit(raw.getMessage_limit());
    }

    public KrakenBridgeOrderBookConfig convertToKrakenBridgeOrderBookConfig(DbKrakenBridgeOrderBookConfig raw) {
        return new KrakenBridgeOrderBookConfig()
                .setId(raw.getId())
                .setCurrencyPair(currencyResolver.krakenCurrencyPair(raw.getCurrency_base(), raw.getCurrency_counter()))
                .setBatchSize(raw.getBatch_size())
                .setSecsTimeout(raw.getSecs_timeout());
    }

    public MiscConfig convertToMiscConfig(DbMiscConfig raw) {
        return new MiscConfig()
                .setId(raw.getId())
                .setName(raw.getName())
                .setValue(raw.getValue());
    }

    @SneakyThrows
    public CbOrderBook convertToDbKrakenOrderBook(DbKrakenOrderBook input) {
        return new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(input.getExchange_datetime().toInstant())
                .setExchangeDate(input.getExchange_date().toLocalDate())
                .setReceivedMicros(input.getReceived_micros())
                .setBids(DbUtils.doubleMapFromArray(input.getBids()))
                .setAsks(DbUtils.doubleMapFromArray(input.getAsks()));
    }

    public List<CbOrderBook> convertToCbOrderBooks(Collection<XchangeKrakenOrderBook> krakenOrderBooks) {
        return krakenOrderBooks.parallelStream().map(this::convertToCbOrderBook).toList();
    }

    public CbOrderBook convertToCbOrderBook(XchangeKrakenOrderBook krakenOrderBook) {
        OrderBook orderBook = krakenOrderBook.getOrderBook();
        return new CbOrderBook()
                .setSnapshot(true)
                .setExchangeDatetime(orderBook.getTimeStamp().toInstant())
                .setExchangeDate(LocalDate.ofInstant(orderBook.getTimeStamp().toInstant(), ZoneId.systemDefault()))
                .setReceivedMicros(krakenOrderBook.getMicroSeconds())
                .setBids(quoteTreeMapFromLimitOrders(orderBook.getBids()))
                .setAsks(quoteTreeMapFromLimitOrders(orderBook.getAsks()));
    }

    public CbOrderBook convertToCbOrderBook(KrakenOrderBook2Data krakenOrderBookData, boolean snapshot) {
        Instant timestamp = krakenOrderBookData.getTimestamp();
        return new CbOrderBook()
                .setSnapshot(snapshot)
                .setExchangeDatetime(timestamp)
                .setExchangeDate(timestamp == null ? null : LocalDate.ofInstant(timestamp, ZoneId.systemDefault()))
                .setReceivedMicros(TimeUtils.currentMicros())
                .setBids(quoteTreeMapFromLevels(krakenOrderBookData.getBids()))
                .setAsks(quoteTreeMapFromLevels(krakenOrderBookData.getAsks()));
    }

    public DbKrakenOrderBook convertToDbKrakenOrderBook(XchangeKrakenOrderBook krakenOrderBook, Connection connection) {
        OrderBook orderBook = krakenOrderBook.getOrderBook();

        List<LimitOrder> orderBookBids = orderBook.getBids();
        List<LimitOrder> orderBookAsks = orderBook.getAsks();

        List<Pair<Double, Double>> bids = quoteList(orderBookBids);
        List<Pair<Double, Double>> asks = quoteList(orderBookAsks);

        int bidsHash = bids.hashCode();
        int asksHash = asks.hashCode();

        Pair<Double, Double> highestBid = bids.get(0);
        Pair<Double, Double> lowestAsk = asks.get(0);

        Array bidsArray = sqlArray(bids, DbWriteProvider.TYPE_ORDER_BOOK_QUOTE, connection);
        Array asksArray = sqlArray(asks, DbWriteProvider.TYPE_ORDER_BOOK_QUOTE, connection);

        DbKrakenOrderBook result = new DbKrakenOrderBook();
        result.setProcess(krakenOrderBook.getProcess());
        result.setExchange_datetime(new Timestamp(orderBook.getTimeStamp().getTime()));
        result.setExchange_date(new java.sql.Date(orderBook.getTimeStamp().getTime()));
        result.setReceived_micros(krakenOrderBook.getMicroSeconds());
        result.setHighest_bid_price(highestBid.getLeft());
        result.setHighest_bid_volume(highestBid.getRight());
        result.setLowest_ask_price(lowestAsk.getLeft());
        result.setLowest_ask_volume(lowestAsk.getRight());
        result.setBids_hash(bidsHash);
        result.setAsks_hash(asksHash);
        result.setBids(bidsArray);
        result.setAsks(asksArray);
        return result;
    }

    public Map<String, Double> convertToRedisPayload(Collection<CbOrderBook> orderBooks) {
        return orderBooks.parallelStream().collect(Collectors.toMap(gson::toJson, orderbook -> (double)(orderbook.getReceivedMicros()), (a, b)->a));
    }

    public List<Pair<Double, Double>> quoteList(List<LimitOrder> limitOrders) {
        return limitOrders.parallelStream().map(this::priceAndQuantity).toList();
    }

    public TreeMap<Double, Double> quoteTreeMapFromLimitOrders(List<LimitOrder> limitOrders) {
        return limitOrders.parallelStream().collect(Collectors.toMap(limitOrder -> limitOrder.getLimitPrice().doubleValue(), limitOrder -> limitOrder.getOriginalAmount().doubleValue(), (a,b)->a, TreeMap::new));
    }

    public TreeMap<Double, Double> quoteTreeMapFromLevels(List<KrakenOrderBookLevel> levels) {
        return levels.parallelStream().collect(Collectors.toMap(KrakenOrderBookLevel::getPrice, KrakenOrderBookLevel::getQty, (a,b)->a, TreeMap::new));
    }

    @SneakyThrows
    Array sqlArray(List<Pair<Double, Double>> quotes, String arrayName, Connection connection) {
        @SuppressWarnings("unchecked")
        Pair<Double, Double>[] pairArray = quotes.toArray(new Pair[0]);
        return connection.createArrayOf(arrayName, pairArray);
    }

    public Pair<Double, Double> priceAndQuantity(LimitOrder limitOrder) {
        return Pair.of(limitOrder.getLimitPrice().doubleValue(), limitOrder.getOriginalAmount().doubleValue());
    }

    public Object[][] matrix(Collection<DbKrakenOrderBook> orderbooks) {
        /*  TODO: implementing the method as below because for some reason this doesn't work (figure out why):
                return orderbooks.parallelStream().map(orderbook -> new Object[] {orderbook.getExchange_datetime(), orderbook.getExchange_date(), orderbook.getBids(), orderbook.getAsks()}).toArray();
         */
        List<Object[]> list = orderbooks.parallelStream().map(orderbook -> new Object[] {
                orderbook.getProcess(),
                orderbook.getExchange_datetime(),
                orderbook.getExchange_date(),
                orderbook.getReceived_micros(),
                orderbook.getHighest_bid_price(),
                orderbook.getHighest_bid_volume(),
                orderbook.getLowest_ask_price(),
                orderbook.getLowest_ask_volume(),
                orderbook.getBids_hash(),
                orderbook.getAsks_hash(),
                orderbook.getBids(),
                orderbook.getAsks()
        }).toList();
        Object[][] result = new Object[orderbooks.size()][];
        for (int i = 0; i < list.size(); ++i) {
            result[i] = list.get(i);
        }
        return result;
    }

}
