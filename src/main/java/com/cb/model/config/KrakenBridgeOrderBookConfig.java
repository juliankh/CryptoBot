package com.cb.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.knowm.xchange.currency.CurrencyPair;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class KrakenBridgeOrderBookConfig {

    private long id;
    private CurrencyPair currencyPair;
    private int batchSize;
    private int secsTimeout;

}
