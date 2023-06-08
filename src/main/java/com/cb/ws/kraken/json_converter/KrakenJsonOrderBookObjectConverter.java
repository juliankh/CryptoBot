package com.cb.ws.kraken.json_converter;

import com.cb.common.ObjectConverter;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseOrderBook;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;

import java.util.List;

@Getter
public class KrakenJsonOrderBookObjectConverter extends KrakenAbstractJsonObjectConverter {

    private static class JsonIdentifier {
        public static final String SUBSCRIPTION_RESPONSE = "\"method\":\"subscribe\"";
        public static final String ORDER_BOOK = "\"channel\":\"book\"";
    }

    private KrakenSubscriptionResponseOrderBook subscriptionResponse;
    private KrakenOrderBookInfo orderBookInfo;

    @Override
    protected List<Object> customObjects() {
        return Lists.newArrayList(subscriptionResponse, orderBookInfo);
    }

    @Override
    protected void nullifyCustomObjects() {
        subscriptionResponse = null;
        orderBookInfo = null;
    }

    @SneakyThrows
    @Override
    protected boolean parseCustom(String json) {
        if (json.contains(JsonIdentifier.SUBSCRIPTION_RESPONSE)) {
            subscriptionResponse = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenSubscriptionResponseOrderBook.class);
        } else if (json.contains(JsonIdentifier.ORDER_BOOK)) {
            orderBookInfo = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenOrderBookInfo.class);
        } else {
            return false;
        }
        return true;
    }


}
