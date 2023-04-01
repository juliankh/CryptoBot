package com.cb.db;

import org.knowm.xchange.currency.CurrencyPair;

public class TableNameResolver {

    public static void main(String[] args) {
        System.out.println((new TableNameResolver()).postfix(CurrencyPair.BTC_USDT));
    }

    // TODO: unit test
    String postfix(CurrencyPair currencyPair) {
        return "_" + currencyPair.getBase().getCurrencyCode().toLowerCase() + "_" + currencyPair.getCounter().getCurrencyCode().toLowerCase();
    }

}
