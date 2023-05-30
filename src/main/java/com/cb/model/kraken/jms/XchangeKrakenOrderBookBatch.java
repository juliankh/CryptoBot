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
public class XchangeKrakenOrderBookBatch implements Serializable {

    private final CurrencyPair currencyPair;
    private final List<XchangeKrakenOrderBook> orderbooks;

}
