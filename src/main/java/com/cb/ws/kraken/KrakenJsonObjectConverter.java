package com.cb.ws.kraken;

public interface KrakenJsonObjectConverter {

    void parse(String json);
    Class<?> objectTypeParsed();

}
