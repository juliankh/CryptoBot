package com.cb.sandbox.objectsize;

import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.db.ReadOnlyDao;
import com.cb.injection.module.MainModule;
import com.cb.model.CbOrderBook;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ObjSizeExp {

    public static void main(String[] args) {
        Instant start = Instant.now();
        ReadOnlyDao readOnlyDao = MainModule.INJECTOR.getInstance(ReadOnlyDao.class);
        Instant to = Instant.now().minus(30, ChronoUnit.MINUTES);
        Instant from = to.minus(5, ChronoUnit.MINUTES);
        List<CbOrderBook> orderBooks = readOnlyDao.krakenOrderBooks(CurrencyPair.BTC_USDT, from, to);
        Instant end = Instant.now();
        double queryRate = TimeUtils.ratePerSecond(start, end, orderBooks.size());
        System.out.println("Retrieving and converting [" + NumberUtils.numberFormat(orderBooks.size()) + "] items took [" + TimeUtils.durationMessage(start, end) + "] at rate of [" + NumberUtils.numberFormat(queryRate) + "/sec]");
    }

}
