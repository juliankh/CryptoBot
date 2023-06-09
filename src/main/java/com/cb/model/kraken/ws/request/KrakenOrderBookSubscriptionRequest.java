package com.cb.model.kraken.ws.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenOrderBookSubscriptionRequest {

    private String method = "subscribe";
    private Integer req_id;
    private KrakenOrderBookSubscriptionRequestParams params;

}
