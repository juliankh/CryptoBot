package com.cb.db.kraken;

import com.cb.common.CurrencyResolver;
import lombok.RequiredArgsConstructor;
import org.knowm.xchange.currency.CurrencyPair;

@RequiredArgsConstructor
public class KrakenTableNameResolver {

    private final CurrencyResolver currencyResolver;

    public String krakenOrderBookTable(CurrencyPair currencyPair) {
        return "cb.kraken_orderbook" + "_" + currencyResolver.lowerCaseToken(currencyPair, "_");
    }

}
