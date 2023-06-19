package com.cb.processor.kraken;

import com.cb.model.kraken.ws.response.instrument.KrakenAsset;
import com.cb.model.kraken.ws.response.instrument.KrakenAssetPair;
import com.cb.model.kraken.ws.response.instrument.KrakenInstrumentData;
import com.cb.model.kraken.ws.response.instrument.KrakenInstrumentInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseInstrument;
import com.cb.ws.kraken.KrakenJsonInstrumentObjectConverter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class KrakenJsonInstrumentProcessor extends KrakenAbstractJsonProcessor {

    @Inject
    private KrakenJsonInstrumentObjectConverter jsonObjectConverter;

    public void initialize(String driverName, int requestId) {
        super.initialize(driverName, requestId);
    }

    @Override
    public synchronized void process(String json) {
        super.process(json, jsonObjectConverter);
    }

    @Override
    public void processCustom(Class<?> objectType) {
        if (objectType == KrakenSubscriptionResponseInstrument.class) {
            processSubscriptionResponse(jsonObjectConverter.getSubscriptionResponse());
        } else if (objectType == KrakenInstrumentInfo.class) {
            processInstrumentInfo(jsonObjectConverter.getInstrumentInfo());
        } else {
            throw new RuntimeException("Unknown object type parsed: [" + objectType + "]");
        }
    }

    public void processSubscriptionResponse(KrakenSubscriptionResponseInstrument subscriptionResponse) {
        boolean requestIdMatches = requestIdMatches(subscriptionResponse.getReq_id());
        log.info(requestIdMatches ? "": "Got Subscription Response where Request ID returned [" + subscriptionResponse.getReq_id() + "] does not equal the original Request ID [" + requestId + "], so will log the response but otherwise ignore: " + subscriptionResponse);
        if (requestIdMatches && !subscriptionResponse.isSuccess()) {
            throw new RuntimeException("Error when trying to subscribe to Kraken Instrument channel: " + subscriptionResponse);
        }
    }

    public void processInstrumentInfo(KrakenInstrumentInfo instrumentInfo) {
        KrakenInstrumentData data = instrumentInfo.getData();
        List<KrakenAsset> assets = data.getAssets();
        List<KrakenAssetPair> assetPairs = data.getPairs();
        writeDao.upsertKrakenAssets(assets);
        log.info("Upserted [" + assets.size() + "] Kraken Assets");
        writeDao.upsertKrakenAssetPairs(assetPairs);
        log.info("Upserted [" + assetPairs.size() + "] Kraken Asset Pairs");
    }

    @Override
    public void cleanup() {
        log.info("Cleaning up");
        super.cleanup();
    }

}
