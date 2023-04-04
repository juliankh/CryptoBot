package com.cb.db;

import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;

import static org.junit.Assert.assertEquals;

public class TableNameResolverTest {

    private static final TableNameResolver TABLE_NAME_RESOLVER = new TableNameResolver();

    @Test
    public void postfix() {
        assertEquals("_ada_btc", TABLE_NAME_RESOLVER.postfix(CurrencyPair.ADA_BTC));
    }

}
