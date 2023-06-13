package com.cb.common;

import com.cb.common.util.TimeUtils;
import com.cb.db.DbUtils;
import com.cb.db.DbWriteProvider;
import com.cb.model.CbOrderBook;
import com.cb.model.DataProvider;
import com.cb.model.config.*;
import com.cb.model.config.db.*;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.jms.XchangeKrakenOrderBook;
import com.cb.model.kraken.ws.db.DbKrakenAsset;
import com.cb.model.kraken.ws.db.DbKrakenAssetPair;
import com.cb.model.kraken.ws.db.DbKrakenStatusUpdate;
import com.cb.model.kraken.ws.response.instrument.KrakenAsset;
import com.cb.model.kraken.ws.response.instrument.KrakenAssetPair;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBook2Data;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookLevel;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdate;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Singleton
public class ObjectConverter {

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private JsonSerializer jsonSerializer;

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

    public List<DbKrakenStatusUpdate> convertToDbKrakenStatusUpdates(KrakenStatusUpdate update) {
        return update.getData().parallelStream().map(data -> new DbKrakenStatusUpdate()
                .setChannel(update.getChannel())
                .setType(update.getType())
                .setApi_version(data.getApi_version())
                .setConnection_id(data.getConnection_id())
                .setSystem(data.getSystem())
                .setVersion(data.getVersion())).toList();
    }

    public List<DbKrakenAsset> convertToDbKrakenAssets(List<KrakenAsset> krakenAssets) {
        return Optional.ofNullable(krakenAssets).orElse(Collections.emptyList())
                .parallelStream()
                .map(this::convertToDbKrakenAsset)
                .toList();
    }

    public DbKrakenAsset convertToDbKrakenAsset(KrakenAsset krakenAsset) {
        return new DbKrakenAsset()
                .setKraken_id(krakenAsset.getId())
                .setStatus(krakenAsset.getStatus())
                .setPrecision(krakenAsset.getPrecision())
                .setPrecision_display(krakenAsset.getPrecision_display())
                .setBorrowable(krakenAsset.isBorrowable())
                .setCollateral_value(krakenAsset.getCollateral_value())
                .setMargin_rate(krakenAsset.getMargin_rate());
    }

    public List<DbKrakenAssetPair> convertToDbKrakenAssetPairs(List<KrakenAssetPair> krakenAssetPairs) {
        return Optional.ofNullable(krakenAssetPairs).orElse(Collections.emptyList())
                .parallelStream()
                .map(this::convertToDbKrakenAssetPair)
                .toList();
    }

    public DbKrakenAssetPair convertToDbKrakenAssetPair(KrakenAssetPair krakenAssetPair) {
        DbKrakenAssetPair dbAssetPair = new DbKrakenAssetPair();
        dbAssetPair.setSymbol(krakenAssetPair.getSymbol());
        dbAssetPair.setBase(krakenAssetPair.getBase());
        dbAssetPair.setQuote(krakenAssetPair.getQuote());
        dbAssetPair.setStatus(krakenAssetPair.getStatus());
        dbAssetPair.setHas_index(krakenAssetPair.isHas_index());
        dbAssetPair.setMarginable(krakenAssetPair.isMarginable());
        dbAssetPair.setMargin_initial(krakenAssetPair.getMargin_initial());
        dbAssetPair.setPosition_limit_long(krakenAssetPair.getPosition_limit_long());
        dbAssetPair.setPosition_limit_short(krakenAssetPair.getPosition_limit_short());
        dbAssetPair.setQty_min(krakenAssetPair.getQty_min());
        dbAssetPair.setQty_precision(krakenAssetPair.getQty_precision());
        dbAssetPair.setQty_increment(krakenAssetPair.getQty_increment());
        dbAssetPair.setPrice_precision(krakenAssetPair.getPrice_precision());
        dbAssetPair.setPrice_increment(krakenAssetPair.getPrice_increment());
        dbAssetPair.setCost_min(krakenAssetPair.getCost_min());
        dbAssetPair.setCost_precision(krakenAssetPair.getCost_precision());
        return dbAssetPair;
    }

    public List<KrakenAssetPair> convertToKrakenAssetPairs(List<DbKrakenAssetPair> krakenAssetPairs) {
        return Optional.ofNullable(krakenAssetPairs).orElse(Collections.emptyList())
                .parallelStream()
                .map(this::convertToKrakenAssetPair)
                .toList();
    }

    public KrakenAssetPair convertToKrakenAssetPair(DbKrakenAssetPair krakenAssetPair) {
        return new KrakenAssetPair()
                .setSymbol(krakenAssetPair.getSymbol())
                .setBase(krakenAssetPair.getBase())
                .setQuote(krakenAssetPair.getQuote())
                .setStatus(krakenAssetPair.getStatus())
                .setHas_index(krakenAssetPair.isHas_index())
                .setMarginable(krakenAssetPair.isMarginable())
                .setMargin_initial(krakenAssetPair.getMargin_initial())
                .setPosition_limit_long(krakenAssetPair.getPosition_limit_long())
                .setPosition_limit_short(krakenAssetPair.getPosition_limit_short())
                .setQty_min(krakenAssetPair.getQty_min())
                .setQty_precision(krakenAssetPair.getQty_precision())
                .setQty_increment(krakenAssetPair.getQty_increment())
                .setPrice_precision(krakenAssetPair.getPrice_precision())
                .setPrice_increment(krakenAssetPair.getPrice_increment())
                .setCost_min(krakenAssetPair.getCost_min())
                .setCost_precision(krakenAssetPair.getCost_precision());
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
                .setAsks(quoteTreeMapFromLimitOrders(orderBook.getAsks()))
                .setMisc(DataProvider.XCHANGE_KRAKEN.name());
    }

    public CbOrderBook convertToCbOrderBook(KrakenOrderBook2Data krakenOrderBookData, boolean snapshot) {
        Instant timestamp = krakenOrderBookData.getTimestamp();
        CurrencyPair currencyPair = currencyResolver.krakenCurrencyPair(krakenOrderBookData.getSymbol());
        return new CbOrderBook()
                .setCurrencyPair(currencyPair)
                .setSnapshot(snapshot)
                .setExchangeDatetime(timestamp)
                .setExchangeDate(timestamp == null ? null : LocalDate.ofInstant(timestamp, ZoneId.systemDefault()))
                .setReceivedMicros(TimeUtils.currentMicros())
                .setBids(quoteTreeMapFromLevels(krakenOrderBookData.getBids()))
                .setAsks(quoteTreeMapFromLevels(krakenOrderBookData.getAsks()))
                .setChecksum(krakenOrderBookData.getChecksum())
                .setMisc(DataProvider.DIRECT_KRAKEN.name());
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
        return orderBooks.parallelStream().collect(Collectors.toMap(jsonSerializer::serializeToJson, orderbook -> (double)(orderbook.getReceivedMicros()), (a,b)->b));
    }

    public List<Pair<Double, Double>> quoteList(List<LimitOrder> limitOrders) {
        return limitOrders.parallelStream().map(this::priceAndQuantity).toList();
    }

    public TreeMap<Double, Double> quoteTreeMapFromLimitOrders(List<LimitOrder> limitOrders) {
        return limitOrders.parallelStream().collect(Collectors.toMap(limitOrder -> limitOrder.getLimitPrice().doubleValue(), limitOrder -> limitOrder.getOriginalAmount().doubleValue(), (a,b)->b, TreeMap::new));
    }

    public TreeMap<Double, Double> quoteTreeMapFromLevels(List<KrakenOrderBookLevel> levels) {
        return levels.parallelStream().collect(Collectors.toMap(KrakenOrderBookLevel::getPrice, KrakenOrderBookLevel::getQty, (a,b)->b, TreeMap::new));
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

    public <T> Object[][] matrix(Collection<T> items, Function<T, Object[]> converter) {
        List<Object[]> list = items.parallelStream().map(converter).toList();
        Object[][] result = new Object[items.size()][];
        for (int i = 0; i < list.size(); ++i) {
            result[i] = list.get(i);
        }
        return result;
    }

}
