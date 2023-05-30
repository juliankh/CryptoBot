package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class KrakenOrderBook {

    private String channel;
    private String type;
    private List<KrakenOrderBook2Data> data;

    public boolean isSnapshot() {
        return "snapshot".equals(type);
    }

}
