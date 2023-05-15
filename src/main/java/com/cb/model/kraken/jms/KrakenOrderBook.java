package com.cb.model.kraken.jms;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.knowm.xchange.dto.marketdata.OrderBook;

import java.io.Serializable;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenOrderBook implements Serializable {

    private String process;
    private long microSeconds; // micro-seconds (milli = 1 / 1,000; micro = 1 / 1,000,000; nano = 1 / 1,000,000,000)
    private OrderBook orderBook;

}
