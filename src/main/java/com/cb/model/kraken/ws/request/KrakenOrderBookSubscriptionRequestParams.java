package com.cb.model.kraken.ws.request;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
public class KrakenOrderBookSubscriptionRequestParams {

    private String channel = "book";
    private boolean snapshot;
    private int depth;
    private List<String> symbol;

}
