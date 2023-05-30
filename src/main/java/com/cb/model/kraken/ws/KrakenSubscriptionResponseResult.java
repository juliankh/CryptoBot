package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KrakenSubscriptionResponseResult {

    private String channel;
    private int depth;
    private boolean snapshot; // whether to include snapshot as the initial msg
    private String symbol;

}
