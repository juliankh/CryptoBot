package com.cb.db.kraken;

import com.cb.common.CurrencyResolver;
import org.knowm.xchange.currency.CurrencyPair;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class KrakenTableNameResolver {

    @Inject
    private CurrencyResolver currencyResolver;

    public String krakenOrderBookTable(CurrencyPair currencyPair) {
        return "cb.kraken_orderbook" + "_" + currencyResolver.lowerCaseToken(currencyPair, "_");
    }

}
