package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenOrderBookLevel {

    private double price;
    private double qty;

}
