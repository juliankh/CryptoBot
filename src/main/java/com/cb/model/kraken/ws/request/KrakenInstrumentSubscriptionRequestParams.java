package com.cb.model.kraken.ws.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenInstrumentSubscriptionRequestParams {

    private String channel = "instrument";
    private boolean snapshot = true;

}
