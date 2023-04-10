package com.cb.util;

import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;

import static org.junit.Assert.assertEquals;

public class CurrencyResolverTest {

    private static final CurrencyResolver CURRENCY_RESOLVER = new CurrencyResolver();

    @Test
    public void lowerCaseToken() {
        assertEquals("ada_btc", CURRENCY_RESOLVER.lowerCaseToken(CurrencyPair.ADA_BTC, "_"));
    }

    @Test
    public void upperCaseToken() {
        assertEquals("ADA/BTC", CURRENCY_RESOLVER.upperCaseToken(CurrencyPair.ADA_BTC, "/"));
    }

}
