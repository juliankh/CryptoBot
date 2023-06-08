package com.cb.model.kraken.ws.response.subscription;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.time.Instant;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public abstract class KrakenAbstractSubscriptionResponse {

    protected String method;
    protected Integer req_id;
    protected boolean success;
    protected Instant time_in;
    protected Instant time_out;

}
