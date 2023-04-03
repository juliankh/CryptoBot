package com.cb.db;

import com.cb.model.orderbook.DbKrakenOrderbook;
import com.cb.property.CryptoPropertiesDecrypted;
import com.cb.property.CryptoPropertiesDecryptedImpl;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

@Slf4j
@Getter
public class DbProvider {

    public static String TYPE_ORDER_BOOK_QUOTE = "orderbook_quote";

    private QueryRunner queryRunner;
    private Connection readConnection;
    private Connection writeConnection;
    private ObjectConverter objectConverter;
    private TableNameResolver tableNameResolver;

    private BeanListHandler<DbKrakenOrderbook> BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK = new BeanListHandler<>(DbKrakenOrderbook.class);

    public static void main(String[] args) {
        DbProvider dbProvider = new DbProvider();
        IntStream.range(1, 5000).forEach(i -> {
            long firstIndex = i;
            long secondIndex = i + 1;
            List<DbKrakenOrderbook> result = dbProvider.retrieveKrakenOrderBooks(Lists.newArrayList(firstIndex, secondIndex));
            String firstString = result.get(0).toString();
            String secondString = result.get(1).toString();
            if (firstString.equals(secondString)) {
                System.out.println(firstIndex + "-" + secondIndex);
                System.out.println(firstString);
                System.out.println(secondString);
            }
        });
    }

    @SneakyThrows
    public DbProvider() {
        queryRunner = new QueryRunner();
        CryptoPropertiesDecrypted properties = new CryptoPropertiesDecryptedImpl();
        readConnection = DriverManager.getConnection(properties.getDbConnectionUrl(), properties.getReadDbUser(), properties.getReadDbPassword());
        writeConnection = DriverManager.getConnection(properties.getDbConnectionUrl(), properties.getWriteDbUser(), properties.getWriteDbPassword());
        objectConverter = new ObjectConverter();
        tableNameResolver = new TableNameResolver();
    }

    @SneakyThrows
    public List<DbKrakenOrderbook> retrieveKrakenOrderBooks(Collection<Long> ids) {
        String questionMarks = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "SELECT id, exchange_datetime, exchange_date, created, bids, asks FROM cb.kraken_orderbook_btc_usdt WHERE id in (" + questionMarks + ")";
        return queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK, ids.toArray());
    }

    @SneakyThrows
    public void insertKrakenOrderBooks(Collection<OrderBook> orderBooks, CurrencyPair currencyPair, String process) {
        List<DbKrakenOrderbook> convertedBatch = objectConverter.convertToKrakenOrderBooks(orderBooks, writeConnection, process);
        Object[][] payload = objectConverter.matrix(convertedBatch);
        String tableName = "cb.kraken_orderbook" + tableNameResolver.postfix(currencyPair);
        int[] rowCounts = queryRunner.batch(writeConnection,
                "INSERT INTO " + tableName + " (process, exchange_datetime, exchange_date, created, bids_hash, asks_hash, bids, asks) VALUES (?, ?, ?, now(), ?, ?, ?, ?) ON CONFLICT(exchange_datetime, exchange_date, bids_hash, asks_hash) DO NOTHING;",
                payload);

        // TODO: put into a separate method/class
        // check that num of dupe OrderBooks received from upstream is the same as the number of rows skipped insertion into db, and that rowcounts are as expected
        Map<Pair<Integer, Integer>, List<DbKrakenOrderbook>> dupeOrderBooksByHashes = dupeOrderBooks(convertedBatch);
        int numDupes = numDupes(dupeOrderBooksByHashes.values()); // num of dupes received from upstream
        int numSkipped = checkRowCounts(rowCounts); // num of rows that were skipped insertion into db
        log.info("# dupe OrderBooks received from upstream [" + numDupes + "]; # of OrderBook rows skipped insertion [" + numSkipped + "]" + (numDupes == 0 && numSkipped == 0 ? " [NONE]" : "") + (numDupes != numSkipped ? " [DIFFERENT]; (probably due to multiple persisters running concurrently)": ""));
    }

    // TODO: unit test; put into another class
    private int numDupes(Collection<? extends Collection<?>> collections) {
        return collections.stream().mapToInt(list -> list.size() - 1).sum(); // need to subtract because shouldn't count the original, but only the subsequent dupes
    }

    // TODO: unit test; put into another class
    private Map<Pair<Integer, Integer>, List<DbKrakenOrderbook>> dupeOrderBooks(List<DbKrakenOrderbook> convertedBatch) {
        Map<Pair<Integer, Integer>, List<DbKrakenOrderbook>> hashesToOrderBooksMap = convertedBatch
                .parallelStream()
                .collect(groupingBy(dbOrderBook -> Pair.of(dbOrderBook.getBids_hash(), dbOrderBook.getAsks_hash())));
        Map<Pair<Integer, Integer>, List<DbKrakenOrderbook>> dupeOrderBooksByHashes = hashesToOrderBooksMap.entrySet()
                .parallelStream()
                .filter(entry -> entry.getValue().size() > 1)
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
        return dupeOrderBooksByHashes;
    }

    // TODO: add annotation to make this visible to tests; unit test
    private int checkRowCounts(int[] rowCounts) {
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

}
