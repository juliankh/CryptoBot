package com.cb.model.kraken;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.knowm.xchange.currency.CurrencyPair;

import java.util.List;

@Getter
@Setter
@RequiredArgsConstructor
public class OrderBookBatch<T> {

    private final CurrencyPair currencyPair;
    private final List<T> orderbooks;

}
