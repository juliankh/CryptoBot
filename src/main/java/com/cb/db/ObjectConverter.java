package com.cb.db;

import com.cb.model.orderbook.DbKrakenOrderbook;
import lombok.SneakyThrows;
import org.apache.commons.lang3.tuple.Pair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.sql.Array;
import java.sql.Connection;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

public class ObjectConverter {

    public List<DbKrakenOrderbook> convertToKrakenOrderBooks(Collection<OrderBook> orderBooks, Connection connection, String process) {
        return orderBooks.parallelStream().map(orderBook -> convertToKrakenOrderBook(orderBook, connection, process)).toList();
    }

    public DbKrakenOrderbook convertToKrakenOrderBook(OrderBook orderBook, Connection connection, String process) {
        List<LimitOrder> orderBookBids = orderBook.getBids();
        List<LimitOrder> orderBookAsks = orderBook.getAsks();
        LimitOrder highestBidLimitOrder = orderBookBids.get(0);
        LimitOrder lowestAskLimitOrder = orderBookAsks.get(0);
        Array bids = sqlArray(orderBookBids, DbProvider.TYPE_ORDER_BOOK_QUOTE, connection);
        Array asks = sqlArray(orderBookAsks, DbProvider.TYPE_ORDER_BOOK_QUOTE, connection);
        int bidsHash = orderBookBids.hashCode();
        int asksHash = orderBookAsks.hashCode();

        DbKrakenOrderbook result = new DbKrakenOrderbook();
        result.setProcess(process);
        result.setExchange_datetime(new Timestamp(orderBook.getTimeStamp().getTime()));
        result.setExchange_date(new java.sql.Date(orderBook.getTimeStamp().getTime()));
        result.setHighest_bid_price(highestBidLimitOrder.getLimitPrice().doubleValue());
        result.setHighest_bid_volume(highestBidLimitOrder.getOriginalAmount().doubleValue());
        result.setLowest_ask_price(lowestAskLimitOrder.getLimitPrice().doubleValue());
        result.setLowest_ask_volume(lowestAskLimitOrder.getOriginalAmount().doubleValue());
        result.setBids_hash(bidsHash);
        result.setAsks_hash(asksHash);
        result.setBids(bids);
        result.setAsks(asks);
        return result;
    }

    /*
    @SneakyThrows
    private <K,V> Array sqlArray(Map<K,V> map, String arrayName, Connection writeConnection) {
        List<Pair<K,V>> pairList = map.entrySet().stream().map(entry -> Pair.of(entry.getKey(), entry.getValue())).toList();
        Pair<K,V>[] pairArray = pairList.toArray(new Pair[pairList.size()]);
        return writeConnection.createArrayOf(arrayName, pairArray);
    }*/

    @SneakyThrows
    Array sqlArray(List<LimitOrder> quotes, String arrayName, Connection writeConnection) {
        List<Pair<Double, Double>> pairList = quotes.stream().map(quote -> quantityAndPricePair(quote)).toList();
        Pair<Double, Double>[] pairArray = pairList.toArray(new Pair[pairList.size()]);
        return writeConnection.createArrayOf(arrayName, pairArray);
    }

    private Pair<Double, Double> quantityAndPricePair(LimitOrder limitOrder) {
        return Pair.of(limitOrder.getLimitPrice().doubleValue(), limitOrder.getOriginalAmount().doubleValue());
    }

    public Object[][] matrix(List<DbKrakenOrderbook> orderbooks) {
        /*
            implementing the method as below because for some reason this doesn't work:
                return orderbooks.parallelStream().map(orderbook -> new Object[] {orderbook.getExchange_datetime(), orderbook.getExchange_date(), orderbook.getBids(), orderbook.getAsks()}).toArray();
         */
        List<Object[]> list = orderbooks.parallelStream().map(orderbook -> new Object[] {
                orderbook.getProcess(),
                orderbook.getExchange_datetime(),
                orderbook.getExchange_date(),
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
