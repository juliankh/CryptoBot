package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class KrakenStatusUpdateData {

    private String api_version;
    private long connection_id;
    private String system;
    private String version;

}
