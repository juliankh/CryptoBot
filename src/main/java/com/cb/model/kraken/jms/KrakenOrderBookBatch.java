package com.cb.model.kraken.jms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.Serializable;
import java.util.Collection;

@Getter
@Setter
@RequiredArgsConstructor
public class KrakenOrderBookBatch implements Serializable {

    private final CurrencyPair currencyPair;
    private final Collection<KrakenOrderBook> orderbooks;

}
