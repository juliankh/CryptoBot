package com.cb.db.kraken;

import com.cb.common.CurrencyResolver;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.knowm.xchange.currency.CurrencyPair;

@Singleton
public class KrakenTableNameResolver {

    @Inject
    private CurrencyResolver currencyResolver;

    public String krakenOrderBookTable(CurrencyPair currencyPair) {
        return "cb.kraken_orderbook" + "_" + currencyResolver.lowerCaseToken(currencyPair, "_");
    }

}
