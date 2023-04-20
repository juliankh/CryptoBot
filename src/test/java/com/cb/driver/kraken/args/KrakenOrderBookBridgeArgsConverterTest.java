package com.cb.driver.kraken.args;

import org.junit.Test;
import org.knowm.xchange.currency.CurrencyPair;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class KrakenOrderBookBridgeArgsConverterTest {

    @Test
    public void emptyArgs() {
        assertThrows("Args are empty", RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{}));
    }

    @Test
    public void wrongNumOfArgs_TooFew() {
        assertThrows("Number of args [1] != 2", RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"one"}));
    }

    @Test
    public void wrongNumOfArgs_TooMany() {
        assertThrows("Number of args [3] != 2: [one, two, three]", RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"one", "two", "three"}));
    }

    @Test
    public void currencyArgHasWrongDelimiter() {
        assertThrows("1st arg [BTC_USD] should be split into 2 currency parts using delimiter [-]", RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"BTC_USD", "1"}));
    }

    @Test
    public void currency1NotExist() {
        assertThrows("Currency Code [DoesntExist] doesn't exist", RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"DoesntExist-USD", "1"}));
    }

    @Test
    public void currency2NotExist() {
        assertThrows("Currency Code [DoesntExist2] doesn't exist", RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"BTC-DoesntExist2", "1"}));
    }

    @Test
    public void everythingCorrect() {
        KrakenOrderBookBridgeArgsConverter argsConverter = new KrakenOrderBookBridgeArgsConverter(new String[]{"BTC-USD", "1"});
        CurrencyPair currencyPair = argsConverter.getCurrencyPair();
        String driverToken = argsConverter.getDriverToken();
        assertEquals(CurrencyPair.BTC_USD, currencyPair);
        assertEquals("1", driverToken);
    }

}
