package com.cb.processor.kraken;

import com.cb.common.util.GeneralUtils;
import com.cb.model.kraken.ws.response.instrument.KrakenAsset;
import com.cb.model.kraken.ws.response.instrument.KrakenAssetPair;
import com.cb.model.kraken.ws.response.instrument.KrakenInstrumentData;
import com.cb.model.kraken.ws.response.instrument.KrakenInstrumentInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseInstrument;
import com.cb.ws.kraken.json_converter.KrakenJsonInstrumentObjectConverter;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import java.util.List;

@Slf4j
public class KrakenJsonInstrumentProcessor extends KrakenAbstractJsonProcessor {

    @Inject
    private KrakenJsonInstrumentObjectConverter converter;

    @Override
    public synchronized void process(String json) {
        try {
            converter.parse(json);
            Class<?> objectType = converter.objectTypeParsed();
            if (!processCommon(objectType, converter)) {
                processCustom(objectType);
            }
        } catch (Exception e) {
            log.error("Problem processing json: [" + json + "]", e);
            throw new RuntimeException("Problem processing json: [" + GeneralUtils.truncateStringIfNecessary(json, 100) + "]", e);
        }
    }

    public void processCustom(Class<?> objectType) {
        if (objectType == KrakenSubscriptionResponseInstrument.class) {
            processSubscriptionResponse(converter.getSubscriptionResponse());
        } else if (objectType == KrakenInstrumentInfo.class) {
            processInstrumentInfo(converter.getInstrumentInfo());
        } else {
            throw new RuntimeException("Unknown object type parsed: [" + objectType + "]");
        }
    }

    public void processSubscriptionResponse(KrakenSubscriptionResponseInstrument subscriptionResponse) {
        log.info("" + subscriptionResponse);
        if (!subscriptionResponse.isSuccess()) {
            throw new RuntimeException("Error when trying to subscribe to Kraken Instrument channel: " + subscriptionResponse);
        }
    }

    public void processInstrumentInfo(KrakenInstrumentInfo instrumentInfo) {
        KrakenInstrumentData data = instrumentInfo.getData();
        List<KrakenAsset> assets = data.getAssets();
        List<KrakenAssetPair> assetPairs = data.getPairs();
        dbWriteProvider.upsertKrakenAssets(assets);
        log.info("Upserted [" + assets.size() + "] Kraken Assets");
        dbWriteProvider.upsertKrakenAssetPairs(assetPairs);
        log.info("Upserted [" + assetPairs.size() + "] Kraken Asset Pairs");
    }

    @Override
    public void cleanup() {
        log.info("Cleaning up");
        super.cleanup();
    }

}
