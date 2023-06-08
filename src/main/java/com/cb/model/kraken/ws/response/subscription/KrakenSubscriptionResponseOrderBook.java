package com.cb.model.kraken.ws.response.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString(callSuper = true)
public class KrakenSubscriptionResponseOrderBook extends KrakenAbstractSubscriptionResponse {

    private KrakenSubscriptionResponseOrderBookResult result;

}
