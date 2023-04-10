package com.cb.db;

import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.property.CryptoProperties;
import com.cb.util.CurrencyResolver;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.tuple.Triple;
import org.knowm.xchange.currency.CurrencyPair;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Slf4j
@Getter
public class DbProvider {

    public static String TYPE_ORDER_BOOK_QUOTE = "orderbook_quote";

    private static final BeanListHandler<DbKrakenOrderBook> BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK = new BeanListHandler<>(DbKrakenOrderBook.class);

    private final QueryRunner queryRunner;
    private final Connection readConnection;
    private final Connection writeConnection;
    private final ObjectConverter objectConverter;
    private final CurrencyResolver tokenResolver;

    @SneakyThrows
    public DbProvider() {
        queryRunner = new QueryRunner();
        CryptoProperties properties = new CryptoProperties();
        readConnection = DriverManager.getConnection(properties.getDbConnectionUrl(), properties.getReadDbUser(), properties.getReadDbPassword());
        writeConnection = DriverManager.getConnection(properties.getDbConnectionUrl(), properties.getWriteDbUser(), properties.getWriteDbPassword());
        objectConverter = new ObjectConverter();
        tokenResolver = new CurrencyResolver();
    }

    @SneakyThrows
    public List<DbKrakenOrderBook> retrieveKrakenOrderBooks(Collection<Long> ids) {
        String questionMarks = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, exchange_datetime, exchange_date, created, bids, asks FROM cb.kraken_orderbook_btc_usdt WHERE id in (" + questionMarks + ")";
        return queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK, ids.toArray());
    }

    @SneakyThrows
    public void insertKrakenOrderBooks(Collection<DbKrakenOrderBook> orderBooks, CurrencyPair currencyPair) {
        Object[][] payload = objectConverter.matrix(orderBooks);
        String tableName = "cb.kraken_orderbook" + "_" + tokenResolver.lowerCaseToken(currencyPair, "_");
        int[] rowCounts = queryRunner.batch(writeConnection,
                "INSERT INTO " + tableName + " (process, exchange_datetime, exchange_date, received_nanos, created, highest_bid_price, highest_bid_volume, lowest_ask_price, lowest_ask_volume, bids_hash, asks_hash, bids, asks) VALUES (?, ?, ?, ?, now(), ?, ?, ?, ?, ?, ?, ?, ?) ON CONFLICT(received_nanos, bids_hash, asks_hash) DO NOTHING;",
                payload);
        checkDupes(orderBooks, rowCounts);
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

}
