package com.cb.sandbox.examples.ws;

import com.cb.util.TimeUtils;
import info.bitrich.xchangestream.core.StreamingExchange;
import info.bitrich.xchangestream.core.StreamingExchangeFactory;
import info.bitrich.xchangestream.kraken.KrakenStreamingExchange;
import io.reactivex.disposables.Disposable;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class KrakenTradesExample {

    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.ADA, Currency.AUD);
    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.ATOM, Currency.USD);
    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.SOL, Currency.EUR);
    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.SOL, Currency.USD);
    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.XRP, Currency.USD);
    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.BTC, Currency.USDC);
    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.DAI, Currency.USD);
    private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.BTC, Currency.USDT);
    //private static CurrencyPair CURRENCY_PAIR = CurrencyPair.BTC_USD;

  public static void main(String[] args) throws InterruptedException {
    AtomicReference<Long> numTrades = new AtomicReference<>(0L);
    AtomicReference<Double> accumulatedQty = new AtomicReference<>((double) 0);
    Instant start = Instant.now();

    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
    StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
    krakenExchange.connect().blockingAwait();
    Disposable tradesDisp =
        krakenExchange
            .getStreamingMarketDataService()
            .getTrades(CURRENCY_PAIR)
            .subscribe(
                s -> {
                    long latestNumTrades = numTrades.updateAndGet(v -> v + 1L);
                    double latestAccumulatedQty = accumulatedQty.updateAndGet(v -> v + s.getOriginalAmount().doubleValue());
                    long millisDuration = ChronoUnit.MILLIS.between(start, Instant.now());
                    double dailyRateTrades = latestNumTrades / (double)millisDuration * TimeUtils.DAY;
                    double dailyRateQty = latestAccumulatedQty / (double)millisDuration * TimeUtils.DAY;
                    double avgTradeSize = dailyRateQty / dailyRateTrades;
                    log.info("Accumulated so far {} trades at Daily Rate {}; {} qty at Daily Rate {}; Avg Trade Size {}; {}", latestNumTrades, dailyRateTrades, latestAccumulatedQty, dailyRateQty, avgTradeSize, s);
                },
                throwable -> {
                    log.error("Fail to get ticker {}", throwable.getMessage(), throwable);
                });

    //TimeUnit.SECONDS.sleep(5245);
    TimeUtils.sleepQuietlyForMillis(5 * TimeUtils.DAY);

    tradesDisp.dispose();

    krakenExchange.disconnect().subscribe(() -> log.info("Disconnected"));
  }
}
