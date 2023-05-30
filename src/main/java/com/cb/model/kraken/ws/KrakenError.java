package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class KrakenError {

    private String error;
    private String method;
    private Long req_id;
    private boolean success;
    private Instant time_in;
    private Instant time_out;

}
