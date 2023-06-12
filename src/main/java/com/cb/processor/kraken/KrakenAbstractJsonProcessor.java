package com.cb.processor.kraken;

import com.cb.db.DbWriteProvider;
import com.cb.model.kraken.ws.response.KrakenError;
import com.cb.model.kraken.ws.response.KrakenHeartbeat;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdate;
import com.cb.processor.JsonProcessor;
import com.cb.ws.kraken.json_converter.KrakenAbstractJsonObjectConverter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class KrakenAbstractJsonProcessor implements JsonProcessor {

    @Inject
    protected DbWriteProvider dbWriteProvider;

    protected Instant timeOfLastHeartbeat = Instant.now();
    protected int requestId;

    public void initialize(int requestId) {
        this.requestId = requestId;
    }

    protected boolean requestIdMatches(int requestId) {
        return this.requestId == requestId;
    }

    public boolean processCommon(Class<?> objectType, KrakenAbstractJsonObjectConverter jsonObjectConverter) {
        if (objectType == KrakenStatusUpdate.class) {
            KrakenStatusUpdate statusUpdate = jsonObjectConverter.getStatusUpdate();
            int numDatas = Optional.ofNullable(statusUpdate.getData()).map(List::size).orElse(0);
            log.info("Status Update with [" + numDatas + "] datas: " + statusUpdate);
            dbWriteProvider.insertKrakenStatusUpdate(statusUpdate);
            return true;
        } else if (objectType == KrakenHeartbeat.class) {
            timeOfLastHeartbeat = Instant.now();
            return true;
        } else if (objectType == KrakenError.class) {
            processError(jsonObjectConverter.getError());
        }
        return false;
    }

    public boolean processError(KrakenError error) {
        boolean requestIdMatches = requestIdMatches(error.getReq_id());
        log.error(requestIdMatches ? "": "Got Error where Request ID returned [" + error.getReq_id() + "] does not equal the original Request ID [" + requestId + "], so will log the error but otherwise ignore: " + error);
        if (!requestIdMatches) {
            throw new RuntimeException("Got error from Kraken: " + error);
        }
        return true;
    }

    @Override
    public void cleanup() {
        log.info("Cleaning up");
        dbWriteProvider.cleanup();
    }

}