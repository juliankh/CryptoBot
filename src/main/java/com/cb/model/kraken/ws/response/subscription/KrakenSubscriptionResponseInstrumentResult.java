package com.cb.model.kraken.ws.response.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KrakenSubscriptionResponseInstrumentResult {

    private String channel;
    private boolean snapshot; // whether to include snapshot as the initial msg

}
