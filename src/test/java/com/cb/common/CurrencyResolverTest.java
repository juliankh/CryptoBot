package com.cb.common;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.currency.CurrencyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

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

    @Test
    public void checkKrakenCurrencyExists_Exists() {
        // Expect no exception
        CURRENCY_RESOLVER.checkKrakenCurrencyExists("ADA");
    }

    @Test
    public void checkKrakenCurrencyExists_NotExists() {
        String currencyCode = "Not Exists!!!";
        RuntimeException exception = assertThrows(RuntimeException.class, () -> CURRENCY_RESOLVER.checkKrakenCurrencyExists(currencyCode));
        assertEquals("There is no Kraken Currency for [" + currencyCode + "]", exception.getMessage());
    }

    @Test
    public void krakenCurrencyPair() {
        assertEquals(CurrencyPair.LTC_BTC, CURRENCY_RESOLVER.krakenCurrencyPair("LTC", "BTC"));
    }

    @Test
    public void krakenCurrencyPair_StringWithSeparator() {
        assertEquals(CurrencyPair.LTC_BTC, CURRENCY_RESOLVER.krakenCurrencyPair("LTC/BTC", '/'));
    }

}
