package com.cb.model.kraken.ws.response.status;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.List;

// TODO: add process column (ie "Direct Kraken OB (BTC-USDT) 05") to here and in db
@Getter
@Setter
@Accessors(chain = true)
@ToString
public class KrakenStatusUpdate {

    private String channel;
    private String type;
    private List<KrakenStatusUpdateData> data;

}
