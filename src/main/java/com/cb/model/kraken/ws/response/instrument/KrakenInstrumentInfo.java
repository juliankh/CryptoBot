package com.cb.model.kraken.ws.response.instrument;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain=true)
public class KrakenInstrumentInfo {

    private String channel;
    private String type;
    private KrakenInstrumentData data;

    // TODO: what does an instrument update look like?
    public boolean isSnapshot() {
        return "snapshot".equalsIgnoreCase(type);
    }

}
