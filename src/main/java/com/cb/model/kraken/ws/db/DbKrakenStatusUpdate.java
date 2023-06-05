package com.cb.model.kraken.ws.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigInteger;

@Getter
@Setter
@Accessors(chain = true) // TODO: will be a problem for DbUtils?
@NoArgsConstructor
@AllArgsConstructor
public class DbKrakenStatusUpdate {

    private long id;
    private String channel;
    private String type;
    private String api_version;
    private BigInteger connection_id;
    private String system;
    private String version;

}
