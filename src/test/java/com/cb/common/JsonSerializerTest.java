package com.cb.common;

import com.cb.common.util.TimeUtils;
import com.cb.injection.module.MainModule;
import com.cb.model.CbOrderBook;
import org.junit.jupiter.api.Test;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.*;
import java.util.TreeMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSerializerTest {

    private final JsonSerializer jsonSerializer = MainModule.INJECTOR.getInstance(JsonSerializer.class);

    @Test
    public void toJson() {
        // setup data
        CurrencyPair currencyPair = CurrencyPair.LTC_USD;

        int year = 1995;
        Month month = Month.APRIL;
        int dayOfMonth = 8;
        int hour = 8;
        int minute = 30;
        int seconds = 45;
        ZoneId zoneId = ZoneOffset.systemDefault();

        Instant exchangeDateTime = TimeUtils.instant(year, month, dayOfMonth, hour, minute, seconds, zoneId);
        LocalDate exchangeDate = LocalDate.of(year, month, dayOfMonth);
        long micros = 123456;

        TreeMap<Double, Double> bids = new TreeMap<>();
        bids.put(10.1, 0.5);
        bids.put(10.2, 1.77);
        bids.put(10.3, 0.9);
        TreeMap<Double, Double> asks = new TreeMap<>();
        asks.put(10.5, 1.89);
        asks.put(10.6, 54.899);
        asks.put(10.7, 21.7);

        long checksum = 145897;
        String misc = "misc1234";

        CbOrderBook orderBook = new CbOrderBook()
                .setCurrencyPair(currencyPair)
                .setSnapshot(true)
                .setExchangeDatetime(exchangeDateTime)
                .setExchangeDate(exchangeDate)
                .setReceivedMicros(micros)
                .setBids(bids)
                .setAsks(asks)
                .setChecksum(checksum)
                .setMisc(misc);

        // engage test
        String result = jsonSerializer.serializeToJson(orderBook);

        // verify
        long expectedExchangeDateTimeMillis = exchangeDateTime.toEpochMilli();
        assertEquals("{\"currencyPair\":\"LTC/USD\",\"snapshot\":true,\"exchangeDatetime\":" + expectedExchangeDateTimeMillis + ",\"exchangeDate\":[1995,4,8],\"receivedMicros\":" + micros + ",\"bids\":{\"10.1\":0.5,\"10.2\":1.77,\"10.3\":0.9},\"asks\":{\"10.5\":1.89,\"10.6\":54.899,\"10.7\":21.7},\"checksum\":" + checksum + ",\"misc\":\"misc1234\"}", result);
    }

}
