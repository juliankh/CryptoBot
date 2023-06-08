package com.cb.model.kraken.ws.response.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KrakenSubscriptionResponseOrderBookResult {

    private String channel;
    private int depth;
    private boolean snapshot; // whether to include snapshot as the initial msg
    private String symbol;

}
