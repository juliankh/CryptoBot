package com.cb.sandbox;

import com.cb.model.orderbook.DbKrakenOrderbook;
import com.cb.property.CryptoProperties;
import com.google.common.collect.Lists;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.sql.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Experiment {

    public static void main(String[] args) throws ClassNotFoundException, SQLException, IOException {

        CryptoProperties props = new CryptoProperties();
        Connection readConnection = DriverManager.getConnection(props.getDbConnectionUrl(), props.getReadDbUser(), props.getReadDbPassword());
        Connection writeConnection = DriverManager.getConnection(props.getDbConnectionUrl(), props.getWriteDbUser(), props.getWriteDbPassword());

        QueryRunner runner = new QueryRunner();
/*
        BeanListHandler<Employee> beanListHandler = new BeanListHandler<>(Employee.class);
        List<Employee> employeeList = runner.query(readConnection, "SELECT * FROM cb.employee", beanListHandler);
        System.out.println(employeeList.stream().map(Object::toString).collect(Collectors.joining("\n")));

        ScalarHandler<Long> scalarHandler = new ScalarHandler<>();
        long newId = runner.insert(writeConnection, "INSERT INTO cb.employee (first_name, last_name, salary, hired_date) VALUES (?, ?, ?, ?)", scalarHandler, "Jenny", "Medici", 60000.60, new java.sql.Date(System.currentTimeMillis()));
        System.out.println(newId);

        int[] rowCounts = runner.batch(writeConnection, "INSERT INTO cb.employee (first_name, last_name, salary, hired_date) VALUES (?, ?, ?, ?)",
                new Object[][] {
                        {"Jenny1", "Medici1", 60001.60, new java.sql.Date(System.currentTimeMillis())},
                        {"Jenny2", "Medici2", 60002.60, new java.sql.Date(System.currentTimeMillis())},
                        {"Jenny3", "Medici3", 60003.60, new java.sql.Date(System.currentTimeMillis())}});
        System.out.println(Arrays.stream(rowCounts).boxed().collect(Collectors.toList()));
*/
        List<Pair<Double, Double>> bids = Lists.newArrayList(Pair.of(1.0, 1.0), Pair.of(2.0, 2.0));
        List<Pair<Double, Double>> asks = Lists.newArrayList(Pair.of(11.0, 11.0), Pair.of(12.0, 12.0));

        Pair<Double, Double>[] bidsArray = bids.toArray(new Pair[bids.size()]);
        Pair<Double, Double>[] asksArray = asks.toArray(new Pair[asks.size()]);

        Array bidsSqlArray = writeConnection.createArrayOf("orderbook_quote", bidsArray);
        Array asksSqlArray = writeConnection.createArrayOf("orderbook_quote", asksArray);

        int[] rowCounts2 = runner.batch(writeConnection, "INSERT INTO cb.kraken_orderbook_btc_usdt (exchange_datetime, exchange_date, bids, asks, created) VALUES (?, ?, ?, ?, now())",
                new Object[][] {
                        {new Timestamp(System.currentTimeMillis()), new java.sql.Date(System.currentTimeMillis()), bidsSqlArray, asksSqlArray},
                        {new Timestamp(System.currentTimeMillis() + 1), new java.sql.Date(System.currentTimeMillis()), bidsSqlArray, asksSqlArray}});
        System.out.println(Arrays.stream(rowCounts2).boxed().collect(Collectors.toList()));

        BeanListHandler<DbKrakenOrderbook> krakenorderBookListHandler = new BeanListHandler<>(DbKrakenOrderbook.class);
        List<DbKrakenOrderbook> krakenOrderbookBtcUsdList = runner.query(readConnection, "SELECT id, exchange_datetime, exchange_date, bids, asks, created FROM cb.kraken_orderbook_btc_usdt", krakenorderBookListHandler);
        System.out.println(krakenOrderbookBtcUsdList.size());
    }

    public static <K,V> Array sqlArray(Map<K,V> map, String arrayName, Connection writeConnection) throws SQLException {
        List<Pair<K,V>> pairList = map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList();
        Pair<K,V>[] pairArray = pairList.toArray(new Pair[pairList.size()]);
        return writeConnection.createArrayOf(arrayName, pairArray);
    }

}
