package com.cb.db;

import com.cb.model.orderbook.DbKrakenOrderbook;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ObjectConverter {

    public List<DbKrakenOrderbook> convertToKrakenOrderBooks(Collection<OrderBook> orderBooks, Connection connection, String process) {
        return orderBooks.parallelStream().map(orderBook -> convertToKrakenOrderBook(orderBook, connection, process)).toList();
    }

    // TODO: unit test
    public DbKrakenOrderbook convertToKrakenOrderBook(OrderBook orderBook, Connection connection, String process) {
        Array bids = sqlArray(orderBook.getBids(), DbProvider.TYPE_ORDER_BOOK_QUOTE, connection);
        Array asks = sqlArray(orderBook.getAsks(), DbProvider.TYPE_ORDER_BOOK_QUOTE, connection);
        int bidsHash = orderBook.getBids().hashCode();
        int asksHash = orderBook.getAsks().hashCode();
        DbKrakenOrderbook result = new DbKrakenOrderbook();
        result.setProcess(process);
        result.setExchange_datetime(new Timestamp(orderBook.getTimeStamp().getTime()));
        result.setExchange_date(new java.sql.Date(orderBook.getTimeStamp().getTime()));
        result.setBids_hash(bidsHash);
        result.setAsks_hash(asksHash);
        result.setBids(bids);
        result.setAsks(asks);
        return result;
    }

    // TODO: annotate so that this is visible only during testing; test
    @SneakyThrows
    private <K,V> Array sqlArray(Map<K,V> map, String arrayName, Connection writeConnection) {
        List<Pair<K,V>> pairList = map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList();
        Pair<K,V>[] pairArray = pairList.toArray(new Pair[pairList.size()]);
        return writeConnection.createArrayOf(arrayName, pairArray);
    }

    // TODO: annotate so that this is visible only during testing; test
    @SneakyThrows
    private Array sqlArray(List<LimitOrder> quotes, String arrayName, Connection writeConnection) {
        List<Pair<Double, Double>> pairList = quotes.stream().map(quote -> Pair.of(quote.getLimitPrice().doubleValue(), quote.getOriginalAmount().doubleValue())).toList();
        Pair<Double, Double>[] pairArray = pairList.toArray(new Pair[pairList.size()]);
        return writeConnection.createArrayOf(arrayName, pairArray);
    }

    // TODO: unit test
    public Object[][] matrix(List<DbKrakenOrderbook> orderbooks) {
        /*
            implementing the method as below because for some reason this doesn't work:
                return orderbooks.parallelStream().map(orderbook -> new Object[] {orderbook.getExchange_datetime(), orderbook.getExchange_date(), orderbook.getBids(), orderbook.getAsks()}).toArray();
         */
        List<Object[]> list = orderbooks.parallelStream().map(orderbook -> new Object[] {
                orderbook.getProcess(),
                orderbook.getExchange_datetime(),
                orderbook.getExchange_date(),
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
