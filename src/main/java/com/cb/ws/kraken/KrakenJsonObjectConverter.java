package com.cb.ws.kraken;

import com.cb.model.kraken.ws.response.KrakenError;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdate;

public interface KrakenJsonObjectConverter {

    void parse(String json);
    Class<?> objectTypeParsed();
    KrakenStatusUpdate getStatusUpdate();
    KrakenError getError();

}
