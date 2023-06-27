package com.cb.processor.kraken;

import com.cb.alert.Alerter;
import com.cb.db.WriteDao;
import com.cb.exception.ChecksumException;
import com.cb.model.kraken.ws.response.KrakenError;
import com.cb.model.kraken.ws.response.KrakenHeartbeat;
import com.cb.model.kraken.ws.response.status.KrakenStatusUpdate;
import com.cb.processor.JsonProcessor;
import com.cb.ws.kraken.KrakenJsonObjectConverter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Slf4j
public abstract class KrakenAbstractJsonProcessor implements JsonProcessor {

    @Inject
    protected Alerter alerter;

    @Inject
    protected WriteDao writeDao;

    protected String driverName;
    protected Instant timeOfLastHeartbeat = Instant.now();
    protected int requestId;

    public void initialize(String driverName, int requestId) {
        this.driverName = driverName;
        this.requestId = requestId;
    }

    protected boolean requestIdMatches(int requestId) {
        return this.requestId == requestId;
    }

    public synchronized void process(String json, KrakenJsonObjectConverter jsonObjectConverter) {
        try {
            jsonObjectConverter.parse(json);
            Class<?> objectType = jsonObjectConverter.objectTypeParsed();
            if (!processCommon(objectType, jsonObjectConverter)) {
                processCustom(objectType);
            }
        } catch (ChecksumException e) {
            throw e;
        } catch (Exception e) {
            log.error("Problem processing json: [" + json + "].  Logging and sending email alert, but otherwise continuing.", e);
            alerter.sendEmailAlertQuietly(driverName + ": Problem w/json", json, e);
        }
    }

    public boolean processCommon(Class<?> objectType, KrakenJsonObjectConverter jsonObjectConverter) {
        if (objectType == KrakenStatusUpdate.class) {
            KrakenStatusUpdate statusUpdate = jsonObjectConverter.getStatusUpdate();
            int numDatas = Optional.ofNullable(statusUpdate.getData()).map(List::size).orElse(0);
            log.info("Status Update with [" + numDatas + "] datas: " + statusUpdate);
            writeDao.insertKrakenStatusUpdate(statusUpdate);
            return true;
        } else if (objectType == KrakenHeartbeat.class) {
            timeOfLastHeartbeat = Instant.now();
            //log.debug("Heartbeat: " + jsonObjectConverter.getHeartbeat());
            return true;
        } else if (objectType == KrakenError.class) {
            processError(jsonObjectConverter.getError());
        }
        return false;
    }

    public abstract void processCustom(Class<?> objectType);

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
        writeDao.cleanup();
    }

}
