package com.cb.ws.kraken;

import com.cb.common.ObjectConverter;
import com.cb.model.kraken.ws.*;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

// TODO: merge this with ObjectConverter (or somehow resolve the fact that there are 2 object converters)
@Getter
public class KrakenJsonToObjectConverter {

    private static class JsonIdentifier {
        public static final String ERROR = "error";
        public static final String STATUS_UPDATE = "\"channel\":\"status\"";
        public static final String HEARTBEAT = "\"channel\":\"heartbeat\"";
        public static final String SUBSCRIPTION_RESPONSE = "\"method\":\"subscribe\"";
        public static final String ORDER_BOOK = "\"channel\":\"book\"";
    }

    private KrakenError error;
    private KrakenStatusUpdate statusUpdate;
    private KrakenHeartbeat heartbeat;
    private KrakenSubscriptionResponse subscriptionResponse;
    private KrakenOrderBook orderBook;

    public static void main(String[] args) {
        String jsonError = "{\"error\":\"Subscription depth not supported\",\"method\":\"subscribe\",\"req_id\":1234567890,\"success\":false,\"time_in\":\"2023-05-28T01:03:37.445243Z\",\"time_out\":\"2023-05-28T01:03:37.445279Z\"}";
        //String jsonStatusUpdate = "{\"channel\":\"status\",\"data\":[{\"api_version\":\"v2\",\"connection_id\":8952852591826402279,\"system\":\"online\",\"version\":\"2.0.0\"}],\"type\":\"update\"}";
        //String jsonHeartbeat = "{\"channel\":\"heartbeat\"}";
        //String jsonSubscriptionResponse = "{\"method\":\"subscribe\",\"req_id\":1234567890,\"result\":{\"channel\":\"book\",\"depth\":10,\"snapshot\":true,\"symbol\":\"BTC/USD\"},\"success\":true,\"time_in\":\"2023-05-27T21:03:08.413713Z\",\"time_out\":\"2023-05-27T21:03:08.413753Z\"}";
        //String jsonOrderBookSnapshot = "{\"channel\":\"book\",\"type\":\"snapshot\",\"data\":[{\"symbol\":\"BTC/USD\",\"bids\":[{\"price\":26807.9,\"qty\":2.41988861},{\"price\":26807.3,\"qty\":0.00105605},{\"price\":26807.2,\"qty\":0.04659203},{\"price\":26807.0,\"qty\":2.89373301},{\"price\":26806.9,\"qty\":2.61104515},{\"price\":26806.8,\"qty\":2.79779464},{\"price\":26805.7,\"qty\":0.07159344},{\"price\":26805.2,\"qty\":0.04680000},{\"price\":26804.9,\"qty\":0.08951676},{\"price\":26804.8,\"qty\":1.41941983}],\"asks\":[{\"price\":26808.0,\"qty\":0.25325256},{\"price\":26812.3,\"qty\":0.01523107},{\"price\":26815.4,\"qty\":0.07959194},{\"price\":26815.5,\"qty\":0.55956160},{\"price\":26815.9,\"qty\":2.79685245},{\"price\":26816.0,\"qty\":0.93585796},{\"price\":26816.3,\"qty\":0.55952612},{\"price\":26817.4,\"qty\":0.65063008},{\"price\":26819.5,\"qty\":0.15000010},{\"price\":26819.6,\"qty\":0.04680000}],\"checksum\":4171994782}]}";
        //String jsonOrderBookUpdateBids = "{\"channel\":\"book\",\"type\":\"update\",\"data\":[{\"symbol\":\"BTC/USD\",\"bids\":[{\"price\":27013.4,\"qty\":2.77639575}],\"asks\":[],\"checksum\":306164425,\"timestamp\":\"2023-05-28T00:42:36.690934Z\"}]}";
        //String jsonOrderBookUpdateAsks = "{\"channel\":\"book\",\"type\":\"update\",\"data\":[{\"symbol\":\"BTC/USD\",\"bids\":[],\"asks\":[{\"price\":27036.2,\"qty\":0.74450000}],\"checksum\":484646001,\"timestamp\":\"2023-05-28T00:42:36.707767Z\"}]}";

        KrakenJsonToObjectConverter converter = new KrakenJsonToObjectConverter();
        converter.parseJson(jsonError);

        Class<?> objectTypeParsed = converter.objectTypeParsed();

        KrakenError error = converter.getError();
        KrakenStatusUpdate statusUpdate = converter.getStatusUpdate();
        KrakenHeartbeat heartbeat = converter.getHeartbeat();
        KrakenSubscriptionResponse subscriptionResponse = converter.getSubscriptionResponse();
        KrakenOrderBook orderBook = converter.getOrderBook();

        System.out.println();
    }

    // TODO: Reconnect logic: have not received any data for some time (ie 60 secs), nor a heartbeat message for some time (ie 10 secs)

    @SneakyThrows
    public void parseJson(String json) {
        if (StringUtils.isBlank(json)) {
            throw new RuntimeException("JSON is empty: [" + json + "]");
        }

        // starting from scratch, so set all objects to null that may have been parsed before
        error = null;
        statusUpdate = null;
        heartbeat = null;
        subscriptionResponse = null;
        orderBook = null;

        // parse json
        if (json.contains(JsonIdentifier.ERROR)){
            error = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenError.class);
        } else if (json.contains(JsonIdentifier.STATUS_UPDATE)) {
            statusUpdate = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenStatusUpdate.class);
        } else if (json.contains(JsonIdentifier.HEARTBEAT)){
            heartbeat = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenHeartbeat.class);
        } else if (json.contains(JsonIdentifier.SUBSCRIPTION_RESPONSE)){
            subscriptionResponse = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenSubscriptionResponse.class);
        } else if (json.contains(JsonIdentifier.ORDER_BOOK)){
            orderBook = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenOrderBook.class);
        } else {
            throw new RuntimeException("Don't know how to parse this json: <" + json + ">");
        }
    }

    public Class<?> objectTypeParsed() {
        return ObjectUtils.firstNonNull(error, statusUpdate, heartbeat, subscriptionResponse, orderBook).getClass();
    }

}
