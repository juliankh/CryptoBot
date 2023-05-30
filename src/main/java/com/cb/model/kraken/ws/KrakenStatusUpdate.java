package com.cb.model.kraken.ws;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

@Getter
@Setter
@ToString
public class KrakenStatusUpdate {

    private String channel;
    private String type;
    private List<KrakenStatusUpdateData> data;

}
