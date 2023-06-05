package com.cb.db;

import com.cb.common.util.TimeUtils;
import com.cb.model.config.db.*;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.ws.KrakenStatusUpdate;
import com.cb.model.kraken.ws.db.DbKrakenStatusUpdate;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.Getter;
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
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cb.injection.BindingName.DB_WRITE_CONNECTION;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Getter
public class DbWriteProvider extends AbstractDbProvider {

    public static String TYPE_ORDER_BOOK_QUOTE = "orderbook_quote";

    public static final Function<DbKrakenStatusUpdate, Object[]> DB_KRAKEN_STATUS_UPDATE_CONVERTER = dbStatusUpdate -> new Object[] {
            dbStatusUpdate.getChannel(),
            dbStatusUpdate.getType(),
            dbStatusUpdate.getApi_version(),
            dbStatusUpdate.getConnection_id(),
            dbStatusUpdate.getSystem(),
            dbStatusUpdate.getVersion()
    };

    public static final Function<DbKrakenOrderBook, Object[]> DB_KRAKEN_ORDER_BOOK_CONVERTER = orderbook -> new Object[] {
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
    };

    private static final BeanListHandler<DbKrakenOrderBook> BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK = new BeanListHandler<>(DbKrakenOrderBook.class);
    private static final BeanListHandler<DbRedisDataAgeMonitorConfig> BEAN_LIST_HANDLER_DATA_AGE_MONITOR_CONFIG = new BeanListHandler<>(DbRedisDataAgeMonitorConfig.class);
    private static final BeanListHandler<DbRedisDataCleanerConfig> BEAN_LIST_HANDLER_DATA_CLEANER_CONFIG = new BeanListHandler<>(DbRedisDataCleanerConfig.class);
    private static final BeanListHandler<DbQueueMonitorConfig> BEAN_LIST_HANDLER_QUEUE_MONITOR_CONFIG = new BeanListHandler<>(DbQueueMonitorConfig.class);
    private static final BeanListHandler<DbKrakenBridgeOrderBookConfig> BEAN_LIST_HANDLER_KRAKEN_BRIDGE_ORDERBOOK_CONFIG = new BeanListHandler<>(DbKrakenBridgeOrderBookConfig.class);
    private static final BeanListHandler<DbMiscConfig> BEAN_LIST_HANDLER_MISC_CONFIG = new BeanListHandler<>(DbMiscConfig.class);

    private static final ScalarHandler<Timestamp> TIMESTAMP_SCALAR_HANDLER = new ScalarHandler<>();

    @Inject
    @Named(DB_WRITE_CONNECTION)
    private Connection writeConnection;

    @Inject
    private QueryRunner queryRunner;

    public long prune(String table, String column, int hoursLimit) {
        try {
            return TimeUtils.runTimedCallable_NumberedOutput(() -> queryRunner.update(writeConnection, "DELETE FROM " + table + " WHERE " + column + " < NOW() - INTERVAL '" + hoursLimit + " hours';"), "Updating/Deleting", table);
        } catch (Exception e) {
            throw new RuntimeException("Problem pruning table [" + table + "] using column [" + column + "] older than [" + hoursLimit + "] hours", e);
        }
    }

    public void insertKrakenStatusUpdate(KrakenStatusUpdate statusUpdate) {
        List<DbKrakenStatusUpdate> dbStatusUpdates = objectConverter.convertToDbKrakenStatusUpdates(statusUpdate);
        try {
            Object[][] payload = objectConverter.matrix(dbStatusUpdates, DB_KRAKEN_STATUS_UPDATE_CONVERTER);
            queryRunner.batch(writeConnection, "INSERT INTO cb.kraken_status_update (channel, type, api_version, connection_id, system, version) VALUES (?,?,?,?,?,?);", payload);
        } catch (Exception e) {
            throw new RuntimeException("Problem inserting [" + dbStatusUpdates.size() + "] DbKrakenStatusUpdates", e);
        }
    }

    public void insertKrakenOrderBooks(Collection<DbKrakenOrderBook> orderBooks, CurrencyPair currencyPair) {
        try {
            Object[][] payload = objectConverter.matrix(orderBooks, DB_KRAKEN_ORDER_BOOK_CONVERTER);
            String tableName = krakenTableNameResolver.krakenOrderBookTable(currencyPair);
            int[] rowCounts = queryRunner.batch(writeConnection,
                    "INSERT INTO " + tableName + " (process, exchange_datetime, exchange_date, received_micros, created, highest_bid_price, highest_bid_volume, lowest_ask_price, lowest_ask_volume, bids_hash, asks_hash, bids, asks) VALUES (?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(received_micros, bids_hash, asks_hash) DO NOTHING;",
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
                .collect(groupingBy(dbOrderBook -> Triple.of(dbOrderBook.getReceived_micros(), dbOrderBook.getBids_hash(), dbOrderBook.getAsks_hash())));
        Map<Triple<Long, Integer, Integer>, List<DbKrakenOrderBook>> bucketsWithMultipleItemsOnly = buckets.entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return bucketsWithMultipleItemsOnly;
    }

    @SneakyThrows
    public void cleanup() {
        log.info("Cleaning up");
        DbUtils.closeQuietly(writeConnection);
    }

}
