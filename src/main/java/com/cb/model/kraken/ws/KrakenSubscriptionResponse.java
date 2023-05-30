package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.Instant;

@Getter
@Setter
@ToString
public class KrakenSubscriptionResponse {

    private String method;
    private Integer req_id;
    private boolean success;
    private Instant time_in;
    private Instant time_out;
    private KrakenSubscriptionResponseResult result;

}
