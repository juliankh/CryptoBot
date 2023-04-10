package com.cb.model.kraken.jms;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.io.Serializable;

@Getter
@Setter
@RequiredArgsConstructor
public class KrakenOrderBook implements Serializable {

    private final String process;
    private final long secondNanos;
    private final OrderBook orderBook;

}
