package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain=true)
public class KrakenOrderBook {

    private String channel;
    private String type;
    private List<KrakenOrderBook2Data> data;

    public boolean isSnapshot() {
        return "snapshot".equalsIgnoreCase(type);
    }

}
