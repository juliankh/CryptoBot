package com.cb.db;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.db.kraken.KrakenTableNameResolver;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.property.CryptoProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.apache.commons.lang3.tuple.Triple;
import org.knowm.xchange.currency.CurrencyPair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DbProvider {

    public static String TYPE_ORDER_BOOK_QUOTE = "orderbook_quote";

    private static final BeanListHandler<DbKrakenOrderBook> BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK = new BeanListHandler<>(DbKrakenOrderBook.class);
    private static final ScalarHandler<Timestamp> TIMESTAMP_SCALAR_HANDLER = new ScalarHandler<>();

    private final QueryRunner queryRunner;
    private final Connection readConnection;
    private final Connection writeConnection;
    private final ObjectConverter objectConverter;
    private final KrakenTableNameResolver krakenTableNameResolver;

    @SneakyThrows
    public DbProvider() {
        queryRunner = new QueryRunner();
        CryptoProperties properties = new CryptoProperties();
        readConnection = DriverManager.getConnection(properties.dbConnectionUrl(), properties.readDbUser(), properties.readDbPassword());
        writeConnection = DriverManager.getConnection(properties.dbConnectionUrl(), properties.writeDbUser(), properties.writeDbPassword());
        objectConverter = new ObjectConverter();
        krakenTableNameResolver = new KrakenTableNameResolver(new CurrencyResolver());
    }

    public List<CbOrderBook> retrieveKrakenOrderBooks(CurrencyPair currencyPair, Instant from, Instant to) {
        List<DbKrakenOrderBook> dbOrderBooks = retrieveKrakenOrderBooks(currencyPair, Timestamp.from(from), Timestamp.from(to));
        return dbOrderBooks.stream().map(objectConverter::convertToKrakenOrderBook).toList();
    }

    public List<DbKrakenOrderBook> retrieveKrakenOrderBooks(CurrencyPair currencyPair, Timestamp from, Timestamp to) {
        try {
            String tableName = krakenTableNameResolver.krakenOrderBookTable(currencyPair);
            String sql = " SELECT id, process, exchange_datetime, exchange_date, received_nanos, created, highest_bid_price, highest_bid_volume, lowest_ask_price, lowest_ask_volume, bids_hash, asks_hash, bids, asks " +
                         " FROM " + tableName +
                         " WHERE exchange_datetime between ? and ?" +
                         " ORDER BY received_nanos";
            return runTimedQuery(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK, from, to), tableName);
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Kraken OrderBooks for CurrencyPair [" + currencyPair + "] between [" + from + "] and [" + to + "]", e);
        }
    }

    @SneakyThrows
    public Instant timeOfLastItem(String table, String column) {
        try {
            Timestamp result = queryRunner.query(readConnection, "SELECT max(" + column + ") FROM " + table + ";", TIMESTAMP_SCALAR_HANDLER);
            return result.toInstant();
        } catch (Exception e) {
            throw new RuntimeException("Problem getting the time of last item in table [" + table + "] using column [" + column + "]", e);
        }
    }

    public int prune(String table, String column, int daysLimit) {
        try {
            return runTimedUpdate(() -> queryRunner.update(writeConnection, "DELETE FROM " + table + " WHERE " + column + " < NOW() - INTERVAL '" + daysLimit + " days';"), table);
        } catch (Exception e) {
            throw new RuntimeException("Problem pruning table [" + table + "] using column [" + column + "] older than [" + daysLimit + "] days", e);
        }
    }

    @SneakyThrows
    private <T> List<T> runTimedQuery(Callable<List<T>> queryRunner, String itemType) {
        Instant start = Instant.now();
        List<T> result = queryRunner.call();
        Instant end = Instant.now();
        long queryRate = TimeUtils.ratePerSecond(start, end, result.size());
        log.info("Retrieving [" + NumberUtils.NUMBER_FORMAT.format(result.size()) + "] of [" + itemType + "] took [" + TimeUtils.durationMessage(start, end) + "] at rate of [" + NumberUtils.NUMBER_FORMAT.format(queryRate) + "/sec]");
        return result;
    }

    @SneakyThrows
    private int runTimedUpdate(Callable<Integer> queryRunner, String itemType) {
        Instant start = Instant.now();
        int rowcount = queryRunner.call();
        Instant end = Instant.now();
        long queryRate = TimeUtils.ratePerSecond(start, end, rowcount);
        log.info("Updating/Deleting [" + NumberUtils.NUMBER_FORMAT.format(rowcount) + "] of [" + itemType + "] took [" + TimeUtils.durationMessage(start, end) + "] at rate of [" + NumberUtils.NUMBER_FORMAT.format(queryRate) + "/sec]");
        return rowcount;
    }

    public String questionMarks(int numQuestionMarks) {
        return String.join(",", Collections.nCopies(numQuestionMarks, "?"));
    }

    public void insertKrakenOrderBooks(Collection<DbKrakenOrderBook> orderBooks, CurrencyPair currencyPair) {
        try {
            Object[][] payload = objectConverter.matrix(orderBooks);
            String tableName = krakenTableNameResolver.krakenOrderBookTable(currencyPair);
            int[] rowCounts = queryRunner.batch(writeConnection,
                    "INSERT INTO " + tableName + " (process, exchange_datetime, exchange_date, received_nanos, created, highest_bid_price, highest_bid_volume, lowest_ask_price, lowest_ask_volume, bids_hash, asks_hash, bids, asks) VALUES (?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(received_nanos, bids_hash, asks_hash) DO NOTHING;",
                    payload);
            checkDupes(orderBooks, rowCounts);
        } catch (Exception e) {
            throw new RuntimeException("Problem inserting [" + orderBooks.size() + "] DbKrakenOrderBooks for Currency Pair [" + currencyPair + "]", e);
        }
    }

    public void insertJmsDestinationStats(String destinationName, Instant measured, long messages, long consumers) {
        try {
            queryRunner.update(writeConnection, "INSERT INTO cb.jms_destination_stats(name, measured, messages, consumers, created) VALUES (?, ?, ?, ?, now());", destinationName, Timestamp.from(measured), messages, consumers);
        } catch (Exception e) {
            throw new RuntimeException("Problem inserting destination [" + destinationName + "], measured [" + measured + "], messages [" + messages + "], consumers [" + consumers + "]", e);
        }
    }

    // Returns how many rowcounts were 0 (in order to compare to num of dupes received from upstream data source).  If any rowcount > 1, exception is thrown.
    int checkUpsertRowCounts(int[] rowCounts) {
        List<Integer> unexpectedRowCounts = Arrays.stream(rowCounts).filter(rowCount -> rowCount != 1).boxed().toList();
        if (CollectionUtils.isNotEmpty(unexpectedRowCounts)) {
            List<Integer> zeroRowCounts = unexpectedRowCounts.parallelStream().filter(rowCount -> rowCount == 0).toList();
            List<Integer> non0or1RowCounts = unexpectedRowCounts.parallelStream().filter(rowCount -> rowCount != 0).toList();
            if (CollectionUtils.isNotEmpty(non0or1RowCounts)) {
                String non0or1RowCountsString = non0or1RowCounts.stream().map(Object::toString).collect(Collectors.joining(","));
                throw new RuntimeException(non0or1RowCounts.size() + " RowCounts have value different from the expected [0 or 1] after insertion into db: " + non0or1RowCountsString);
            }
            return zeroRowCounts.size(); // the num of dupe rows that weren't inserted
        }
        return 0;
    }

    int numDupes(Collection<? extends Collection<?>> dupeCollections) {
        return dupeCollections.stream().mapToInt(list -> list.size() - 1).sum(); // need to subtract because shouldn't count the original, but only the subsequent dupes
    }

    void checkDupes(Collection<DbKrakenOrderBook> convertedBatch, int[] rowCounts) {
        // check that num of dupe OrderBooks received from upstream is the same as the number of rows skipped insertion into db, and that rowcounts are as expected
        Map<Triple<Long, Integer, Integer>, List<DbKrakenOrderBook>> dupeOrderBooksByHashes = dupeOrderBooks(convertedBatch);
        int numDupes = numDupes(dupeOrderBooksByHashes.values()); // num of dupes received from upstream
        int numSkipped = checkUpsertRowCounts(rowCounts); // num of rows that were skipped insertion into db
        log.info(dupeDescription("OrderBook", numDupes, numSkipped));
    }

    String dupeDescription(String itemType, int numDupes, int numSkipped) {
        return "# dupe " + itemType + " received from upstream [" + numDupes + "]; # of " + itemType + " rows skipped insertion [" + numSkipped + "]" + (numDupes == 0 && numSkipped == 0 ? " [NONE]" : "") + (numDupes != numSkipped ? " [DIFFERENT by (" + (numSkipped - numDupes) + ")] (probably due to multiple persisters running concurrently)": "");
    }

    Map<Triple<Long, Integer, Integer>, List<DbKrakenOrderBook>> dupeOrderBooks(Collection<DbKrakenOrderBook> orderbooks) {
        Map<Triple<Long, Integer, Integer>, List<DbKrakenOrderBook>> buckets = orderbooks
                .parallelStream()
                .collect(groupingBy(dbOrderBook -> Triple.of(dbOrderBook.getReceived_nanos(), dbOrderBook.getBids_hash(), dbOrderBook.getAsks_hash())));
        Map<Triple<Long, Integer, Integer>, List<DbKrakenOrderBook>> bucketsWithMultipleItemsOnly = buckets.entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return bucketsWithMultipleItemsOnly;
    }

    @SneakyThrows
    public void cleanup() {
        log.info("Cleaning up");
        DbUtils.closeQuietly(readConnection);
        DbUtils.closeQuietly(writeConnection);
    }

}
