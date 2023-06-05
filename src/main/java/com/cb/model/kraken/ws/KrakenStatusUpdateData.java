package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.math.BigInteger;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class KrakenStatusUpdateData {

    private String api_version;
    private BigInteger connection_id;
    private String system;
    private String version;

}
