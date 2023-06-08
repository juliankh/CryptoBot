package com.cb.ws.kraken.json_converter;

import com.cb.common.ObjectConverter;
import com.cb.common.util.GeneralUtils;
import com.cb.model.kraken.ws.response.KrakenError;
import com.cb.model.kraken.ws.response.KrakenHeartbeat;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdate;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Objects;

@Getter
public abstract class KrakenAbstractJsonObjectConverter implements KrakenJsonObjectConverter {

    private static class JsonIdentifier {
        public static final String ERROR = "error";
        public static final String STATUS_UPDATE = "\"channel\":\"status\"";
        public static final String HEARTBEAT = "\"channel\":\"heartbeat\"";
    }

    private KrakenError error;
    private KrakenStatusUpdate statusUpdate;
    private KrakenHeartbeat heartbeat;

    @SneakyThrows
    @Override
    public void parse(String json) {
        if (StringUtils.isBlank(json)) {
            throw new RuntimeException("JSON is empty: [" + json + "]");
        }

        // starting from scratch, so set all objects to null that may have been parsed before
        nullifyObjects();

        // parse json
        if (json.contains(JsonIdentifier.ERROR)) {
            error = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenError.class);
        } else if (json.contains(JsonIdentifier.STATUS_UPDATE)) {
            statusUpdate = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenStatusUpdate.class);
        } else if (json.contains(JsonIdentifier.HEARTBEAT)) {
            heartbeat = ObjectConverter.OBJECT_MAPPER.readValue(json, KrakenHeartbeat.class);
        } else {
            if (!parseCustom(json)) {
                throw new RuntimeException("Don't know how to parse this json: <" + GeneralUtils.truncateStringIfNecessary(json, 100) + ">");
            }
        }
    }

    private void nullifyObjects() {
        error = null;
        statusUpdate = null;
        heartbeat = null;
        nullifyCustomObjects();
    }

    @Override
    public Class<?> objectTypeParsed() {
        return ListUtils.union(Lists.newArrayList(error, statusUpdate, heartbeat), customObjects())
                .stream()
                .filter(Objects::nonNull)
                .findFirst()
                .map(Object::getClass)
                .orElse(null);
    }

    protected abstract List<Object> customObjects();
    protected abstract void nullifyCustomObjects();
    protected abstract boolean parseCustom(String json);

}
