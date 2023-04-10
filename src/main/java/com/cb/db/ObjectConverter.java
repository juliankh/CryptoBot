package com.cb.db;

import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.cb.model.kraken.jms.KrakenOrderBook;
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

    public DbKrakenOrderBook convertToKrakenOrderBook(KrakenOrderBook krakenOrderBook, Connection connection) {
        OrderBook orderBook = krakenOrderBook.getOrderBook();

        List<LimitOrder> orderBookBids = orderBook.getBids();
        List<LimitOrder> orderBookAsks = orderBook.getAsks();

        List<Pair<Double, Double>> bids = quotes(orderBookBids);
        List<Pair<Double, Double>> asks = quotes(orderBookAsks);

        int bidsHash = bids.hashCode();
        int asksHash = asks.hashCode();

        Pair<Double, Double> highestBid = bids.get(0);
        Pair<Double, Double> lowestAsk = asks.get(0);

        Array bidsArray = sqlArray(bids, DbProvider.TYPE_ORDER_BOOK_QUOTE, connection);
        Array asksArray = sqlArray(asks, DbProvider.TYPE_ORDER_BOOK_QUOTE, connection);

        DbKrakenOrderBook result = new DbKrakenOrderBook();
        result.setProcess(krakenOrderBook.getProcess());
        result.setExchange_datetime(new Timestamp(orderBook.getTimeStamp().getTime()));
        result.setExchange_date(new java.sql.Date(orderBook.getTimeStamp().getTime()));
        result.setReceived_nanos(krakenOrderBook.getSecondNanos());
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

    public List<Pair<Double, Double>> quotes(List<LimitOrder> limitOrders) {
        return limitOrders.parallelStream().map(this::quantityAndPricePair).toList();
    }

    @SneakyThrows
    Array sqlArray(List<Pair<Double, Double>> quotes, String arrayName, Connection connection) {
        Pair<Double, Double>[] pairArray = quotes.toArray(new Pair[quotes.size()]);
        return connection.createArrayOf(arrayName, pairArray);
    }

    private Pair<Double, Double> quantityAndPricePair(LimitOrder limitOrder) {
        return Pair.of(limitOrder.getLimitPrice().doubleValue(), limitOrder.getOriginalAmount().doubleValue());
    }

    public Object[][] matrix(Collection<DbKrakenOrderBook> orderbooks) {
        /*
            implementing the method as below because for some reason this doesn't work:
                return orderbooks.parallelStream().map(orderbook -> new Object[] {orderbook.getExchange_datetime(), orderbook.getExchange_date(), orderbook.getBids(), orderbook.getAsks()}).toArray();
         */
        List<Object[]> list = orderbooks.parallelStream().map(orderbook -> new Object[] {
                orderbook.getProcess(),
                orderbook.getExchange_datetime(),
                orderbook.getExchange_date(),
                orderbook.getReceived_nanos(),
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
