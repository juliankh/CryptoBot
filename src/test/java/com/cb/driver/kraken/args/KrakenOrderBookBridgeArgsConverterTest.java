package com.cb.driver.kraken.args;

import org.junit.jupiter.api.Test;
import org.knowm.xchange.currency.CurrencyPair;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class KrakenOrderBookBridgeArgsConverterTest {

    @Test
    public void emptyArgs() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{}));
        assertEquals("Args are empty", exception.getMessage());
    }

    @Test
    public void wrongNumOfArgs_TooFew() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"one"}));
        assertEquals("Number of args [1] != 2: [one]", exception.getMessage());
    }

    @Test
    public void wrongNumOfArgs_TooMany() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"one", "two", "three"}));
        assertEquals("Number of args [3] != 2: [one, two, three]", exception.getMessage());
    }

    @Test
    public void currencyArgHasWrongDelimiter() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"BTC_USD", "1"}));
        assertEquals("1st arg [BTC_USD] should be split into 2 currency parts using delimiter [-]", exception.getMessage());
    }

    @Test
    public void currency1NotExist() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"DoesntExist-USD", "1"}));
        assertEquals("Currency Code [DoesntExist] doesn't exist", exception.getMessage());
    }

    @Test
    public void currency2NotExist() {
        RuntimeException exception = assertThrows(RuntimeException.class, () -> new KrakenOrderBookBridgeArgsConverter(new String[]{"BTC-DoesntExist2", "1"}));
        assertEquals("Currency Code [DoesntExist2] doesn't exist", exception.getMessage());
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
