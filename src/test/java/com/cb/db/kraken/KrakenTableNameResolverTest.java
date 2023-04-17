package com.cb.db.kraken;

import com.cb.common.CurrencyResolver;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KrakenTableNameResolverTest {

    @Mock
    private CurrencyResolver currencyResolver;

    @InjectMocks
    private KrakenTableNameResolver krakenTableNameResolver;

    @Before
    public void beforeEachTest() {
        Mockito.reset(currencyResolver);
    }

    @Test
    public void krakenOrderBookTable() {
        CurrencyPair currencyPair = CurrencyPair.LTC_USD;
        String delimiter = "_";
        when(currencyResolver.lowerCaseToken(currencyPair, delimiter)).thenReturn("ltc_usd");
        assertEquals("cb.kraken_orderbook_ltc_usd", krakenTableNameResolver.krakenOrderBookTable(currencyPair));
    }

}
