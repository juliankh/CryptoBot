package com.cb.injection.provider;

import com.cb.common.BufferAggregator;
import com.cb.common.util.GeneralUtils;
import com.cb.processor.kraken.json.KrakenJsonInstrumentProcessor;
import com.cb.ws.WebSocketClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Provider;

@Slf4j
public class KrakenInstrumentWebSocketClientProvider implements Provider<WebSocketClient> {

    @Inject
    private KrakenJsonInstrumentProcessor krakenJsonInstrumentProcessor;

    @Override
    public WebSocketClient get() {
        return new WebSocketClient(new BufferAggregator(), krakenJsonInstrumentProcessor, GeneralUtils.newRandomInt());
    }

}