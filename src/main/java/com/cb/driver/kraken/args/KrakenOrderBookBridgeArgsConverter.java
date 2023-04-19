package com.cb.driver.kraken.args;

import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.Arrays;

@Getter
public class KrakenOrderBookBridgeArgsConverter {

    private static final String CURRENCY_DELIMITER = "-";

    private final String driverToken;
    private final CurrencyPair currencyPair;

    public KrakenOrderBookBridgeArgsConverter(String[] args) {
        if (args == null || args.length == 0) {
            throw new RuntimeException("Args are empty");
        }
        if (args.length != 2) {
            throw new RuntimeException("Number of args [" + args.length + "] != 2: " + Arrays.asList(args));
        }
        if (!args[0].contains(CURRENCY_DELIMITER)) {
            throw new RuntimeException("1st arg [" + args[0] + "] should be split into 2 currency parts using delimiter [" + CURRENCY_DELIMITER + "]");
        }
        String[] tokens = args[0].split(CURRENCY_DELIMITER);
        String currencyToken1 = StringUtils.trim(tokens[0]);
        String currencyToken2 = StringUtils.trim(tokens[1]);
        validateCurrencyCode(currencyToken1);
        validateCurrencyCode(currencyToken2);
        this.driverToken = StringUtils.trim(args[1]);
        Currency firstCurrency = Currency.getInstance(StringUtils.trim(tokens[0]));
        Currency secondCurrency = Currency.getInstance(StringUtils.trim(tokens[1]));
        this.currencyPair = new CurrencyPair(firstCurrency, secondCurrency);
    }

    private void validateCurrencyCode(String code) {
        if (!Currency.getAvailableCurrencyCodes().contains(code)) {
            throw new RuntimeException("Currency Code [" + code + "] doesn't exist");
        }
    }

}
