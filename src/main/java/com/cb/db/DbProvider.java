package com.cb.db;

import com.cb.model.orderbook.DbKrakenOrderbook;
import com.cb.property.CryptoPropertiesDecrypted;
import com.cb.property.CryptoPropertiesDecryptedImpl;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    public void insertKrakenOrderBooks(Collection<OrderBook> orderBooks, CurrencyPair currencyPair) {
        List<DbKrakenOrderbook> convertedBatch = objectConverter.convertToKrakenOrderBooks(orderBooks, currencyPair, writeConnection);
        Object[][] payload = objectConverter.matrix(convertedBatch);
        String tableName = "cb.kraken_orderbook" + tableNameResolver.postfix(currencyPair);
        int[] rowCounts = queryRunner.batch(writeConnection, "INSERT INTO " + tableName + " (exchange_datetime, exchange_date, bids, asks, created) VALUES (?, ?, ?, ?, now())", payload);
        checkRowCounts(rowCounts, 1);
    }

    // TODO: add annotation to make this visible to tests; unit test
    private void checkRowCounts(int[] rowCounts, int expected) {
        List<Integer> unexpectedRowCounts = Arrays.stream(rowCounts).filter(rowCount -> rowCount != expected).boxed().toList();
        if (CollectionUtils.isNotEmpty(unexpectedRowCounts)) {
            String unexpectedRowCountsString = unexpectedRowCounts.stream().map(Object::toString).collect(Collectors.joining(","));
            throw new RuntimeException(unexpectedRowCounts.size() + " RowCounts have value different from the expected [" + expected + "]: " + unexpectedRowCountsString);
        }
    }

}
