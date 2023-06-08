package com.cb.model.kraken.ws.response.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString(callSuper = true)
public class KrakenSubscriptionResponseInstrument extends KrakenAbstractSubscriptionResponse {

    private KrakenSubscriptionResponseInstrumentResult result;

}
