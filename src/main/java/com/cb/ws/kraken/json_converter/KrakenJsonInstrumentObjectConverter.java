package com.cb.ws.kraken.json_converter;

import com.cb.common.JsonSerializer;
import com.cb.model.kraken.ws.response.instrument.KrakenInstrumentInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseInstrument;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;

import javax.inject.Inject;
import java.util.List;

@Getter
public class KrakenJsonInstrumentObjectConverter extends KrakenAbstractJsonObjectConverter {

    @Inject
    private JsonSerializer jsonSerializer;

    private static class JsonIdentifier {
        public static final String SUBSCRIPTION_RESPONSE = "\"method\":\"subscribe\"";
        public static final String INSTRUMENT = "\"channel\":\"instrument\"";
    }

    private KrakenSubscriptionResponseInstrument subscriptionResponse;
    private KrakenInstrumentInfo instrumentInfo;

    @Override
    protected List<Object> customObjects() {
        return Lists.newArrayList(subscriptionResponse, instrumentInfo);
    }

    @Override
    protected void nullifyCustomObjects() {
        subscriptionResponse = null;
        instrumentInfo = null;
    }

    @SneakyThrows
    @Override
    protected boolean parseCustom(String json) {
        if (json.contains(JsonIdentifier.SUBSCRIPTION_RESPONSE)) {
            subscriptionResponse = jsonSerializer.deserializeFromJson(json, KrakenSubscriptionResponseInstrument.class);
        } else if (json.contains(JsonIdentifier.INSTRUMENT)) {
            instrumentInfo = jsonSerializer.deserializeFromJson(json, KrakenInstrumentInfo.class);
        } else {
            return false;
        }
        return true;
    }


}
