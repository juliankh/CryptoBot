package com.cb.ws.kraken.json_converter;

public interface KrakenJsonObjectConverter {

    void parse(String json);
    Class<?> objectTypeParsed();

}
