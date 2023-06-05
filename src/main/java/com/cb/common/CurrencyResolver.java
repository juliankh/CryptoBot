package com.cb.common;

import com.google.inject.Singleton;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

@Singleton
public class CurrencyResolver {

    public static void main(String[] args) {
        System.out.println((new CurrencyResolver()).lowerCaseToken(CurrencyPair.BTC_USDT, "_"));
    }

    public String upperCaseToken(CurrencyPair currencyPair, String separator) {
        return lowerCaseToken(currencyPair, separator).toUpperCase();
    }

    public String lowerCaseToken(CurrencyPair currencyPair, String separator) {
        return currencyPair.getBase().getCurrencyCode().toLowerCase() + separator + currencyPair.getCounter().getCurrencyCode().toLowerCase();
    }

    public CurrencyPair krakenCurrencyPair(String pair) {
        return krakenCurrencyPair(pair, '/');
    }

    public CurrencyPair krakenCurrencyPair(String pair, char separator) {
        String[] parts = StringUtils.split(pair, separator);
        return krakenCurrencyPair(parts[0], parts[1]);
    }

    public CurrencyPair krakenCurrencyPair(String baseCurrencyCode, String counterCurrencyCode) {
        checkKrakenCurrencyExists(baseCurrencyCode);
        checkKrakenCurrencyExists(counterCurrencyCode);
        Currency baseCurrency = Currency.getInstanceNoCreate(baseCurrencyCode);
        Currency counterCurrency = Currency.getInstanceNoCreate(counterCurrencyCode);
        return new CurrencyPair(baseCurrency, counterCurrency);
    }

    public void checkKrakenCurrencyExists(String currencyCode) {
        if (!Currency.getAvailableCurrencyCodes().contains(currencyCode)) {
            throw new RuntimeException("There is no Kraken Currency for [" + currencyCode + "]");
        }
    }

}
