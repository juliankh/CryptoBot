package com.cb.model.kraken.ws.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenInstrumentSubscriptionRequest {

    private String method = "subscribe";
    private Integer req_id;
    private KrakenInstrumentSubscriptionRequestParams params = new KrakenInstrumentSubscriptionRequestParams();

}
