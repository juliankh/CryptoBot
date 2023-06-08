package com.cb.db.kraken;

import com.cb.common.CurrencyResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KrakenTableNameResolverTest {

    @Mock
    private CurrencyResolver currencyResolver;

    @InjectMocks
    private KrakenTableNameResolver krakenTableNameResolver;

    @BeforeEach
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
