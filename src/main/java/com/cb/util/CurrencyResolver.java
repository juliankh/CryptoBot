package com.cb.util;

import org.knowm.xchange.currency.CurrencyPair;

public class CurrencyResolver {

    public static void main(String[] args) {
        System.out.println((new CurrencyResolver()).lowerCaseToken(CurrencyPair.BTC_USDT, "_"));
    }

    public String lowerCaseToken(CurrencyPair currencyPair, String separator) {
        return currencyPair.getBase().getCurrencyCode().toLowerCase() + separator + currencyPair.getCounter().getCurrencyCode().toLowerCase();
    }

    public String upperCaseToken(CurrencyPair currencyPair, String separator) {
        return currencyPair.getBase().getCurrencyCode().toUpperCase() + separator + currencyPair.getCounter().getCurrencyCode().toUpperCase();
    }

}
