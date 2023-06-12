package com.cb.ws.kraken;

import com.cb.common.JsonSerializer;
import com.cb.common.util.GeneralUtils;
import com.cb.model.kraken.ws.response.KrakenError;
import com.cb.model.kraken.ws.response.KrakenHeartbeat;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdate;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.SneakyThrows;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;

@Getter
public abstract class KrakenAbstractJsonObjectConverter implements KrakenJsonObjectConverter {

    @Inject
    private JsonSerializer jsonSerializer;

    private static class JsonIdentifier {
        public static final String ERROR = "error";
        public static final String STATUS_UPDATE = "\"channel\":\"status\"";
        public static final String HEARTBEAT = "\"channel\":\"heartbeat\"";
    }

    protected KrakenError error;
    protected KrakenStatusUpdate statusUpdate;
    protected KrakenHeartbeat heartbeat;

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
            error = jsonSerializer.deserializeFromJson(json, KrakenError.class);
        } else if (json.contains(JsonIdentifier.STATUS_UPDATE)) {
            statusUpdate = jsonSerializer.deserializeFromJson(json, KrakenStatusUpdate.class);
        } else if (json.contains(JsonIdentifier.HEARTBEAT)) {
            heartbeat = jsonSerializer.deserializeFromJson(json, KrakenHeartbeat.class);
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
