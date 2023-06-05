package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class KrakenStatusUpdate {

    private String channel;
    private String type;
    private List<KrakenStatusUpdateData> data;

}
