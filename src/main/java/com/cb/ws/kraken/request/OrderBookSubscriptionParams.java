package com.cb.ws.kraken.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class OrderBookSubscriptionParams {

    private String channel = "book";
    private boolean snapshot;
    private int depth;
    private List<String> symbol;

}
