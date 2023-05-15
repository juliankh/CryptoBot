package com.cb.model.kraken.jms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class KrakenOrderBookBatch implements Serializable {

    private final CurrencyPair currencyPair;
    private final List<KrakenOrderBook> orderbooks; // TODO: decide whether it's better to have here List<CbOrderBook> instead (meaning it's already converted before sending via jms)

}
