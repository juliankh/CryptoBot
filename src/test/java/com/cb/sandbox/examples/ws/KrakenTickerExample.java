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

@Slf4j
public class KrakenTickerExample {

    //private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.ADA, Currency.AUD);
    private static CurrencyPair CURRENCY_PAIR = new CurrencyPair(Currency.SOL, Currency.EUR);

  public static void main(String[] args) throws InterruptedException {
    ExchangeSpecification exchangeSpecification = new ExchangeSpecification(KrakenStreamingExchange.class);
    StreamingExchange krakenExchange = StreamingExchangeFactory.INSTANCE.createExchange(exchangeSpecification);
    krakenExchange.connect().blockingAwait();
    Disposable tickerDis =
        krakenExchange
            .getStreamingMarketDataService()
            .getTicker(CURRENCY_PAIR)
            .subscribe(
                s -> {
                  log.info("Received {}", s);
                },
                throwable -> {
                    log.error("Fail to get ticker {}", throwable.getMessage(), throwable);
                });

    //TimeUnit.SECONDS.sleep(5245);
    TimeUtils.sleepQuietlyForMillis(5 * TimeUtils.DAY);

    tickerDis.dispose();

    krakenExchange.disconnect().subscribe(() -> log.info("Disconnected"));
  }
}
