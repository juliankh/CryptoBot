package com.cb.model.kraken.ws.response.orderbook;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain=true)
public class KrakenOrderBookInfo {

    private String channel;
    private String type;
    private List<KrakenOrderBook2Data> data;

    public boolean isSnapshot() {
        return "snapshot".equalsIgnoreCase(type);
    }

}
