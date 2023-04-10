package com.cb.driver.kraken.args;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.knowm.xchange.currency.CurrencyPair;

import static org.junit.Assert.assertEquals;

public class KrakenOrderBookBridgeArgsConverterTest {

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Test
    public void emptyArgs() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Args are empty");
        new KrakenOrderBookBridgeArgsConverter(new String[]{});
    }

    @Test
    public void wrongNumOfArgs_TooFew() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Number of args [1] != 2");
        new KrakenOrderBookBridgeArgsConverter(new String[]{"one"});
    }

    @Test
    public void wrongNumOfArgs_TooMany() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Number of args [3] != 2");
        new KrakenOrderBookBridgeArgsConverter(new String[]{"one", "two", "three"});
    }

    @Test
    public void currencyArgHasWrongDelimiter() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("1st arg [BTC_USD] should be split into 2 currency parts using delimiter [-]");
        new KrakenOrderBookBridgeArgsConverter(new String[]{"BTC_USD", "1"});
    }

    @Test
    public void currency1NotExist() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Currency Code [DoesntExist] doesn't exist");
        new KrakenOrderBookBridgeArgsConverter(new String[]{"DoesntExist-USD", "1"});
    }

    @Test
    public void currency2NotExist() {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Currency Code [DoesntExist2] doesn't exist");
        new KrakenOrderBookBridgeArgsConverter(new String[]{"BTC-DoesntExist2", "1"});
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
