package com.cb.ws.kraken;

import com.cb.common.ObjectConverter;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.ws.*;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
public class KrakenJsonProcessor {

    private Instant timeOfLastHeartbeat = Instant.now();

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private KrakenJsonToObjectConverter krakenJsonToObjectConverter;

    public synchronized void process(String json) {
        krakenJsonToObjectConverter.parseJson(json);
        Class<?> objectType = krakenJsonToObjectConverter.objectTypeParsed();
        if (objectType == KrakenStatusUpdate.class) {
            KrakenStatusUpdate statusUpdate = krakenJsonToObjectConverter.getStatusUpdate();
            log.info("" + statusUpdate);
            // TODO: save in db
        } else if (objectType == KrakenHeartbeat.class) {
            timeOfLastHeartbeat = Instant.now();
        } else if (objectType == KrakenError.class) {
            KrakenError error = krakenJsonToObjectConverter.getError();
            log.error("" + error);
            throw new RuntimeException("Got error from Kraken: " + error);
        } else if (objectType == KrakenSubscriptionResponse.class) {
            KrakenSubscriptionResponse subscriptionResponse = krakenJsonToObjectConverter.getSubscriptionResponse();
            log.info("" + subscriptionResponse);
            if (!subscriptionResponse.isSuccess()) {
                throw new RuntimeException("Error when trying to subscribe to Kraken: " + subscriptionResponse);
            }
        } else if (objectType == KrakenOrderBook.class) {
            KrakenOrderBook krakenOrderBook = krakenJsonToObjectConverter.getOrderBook();
            // TODO: perhaps put code below into a separate method
            if (krakenOrderBook.isSnapshot()) {
                int numSnapshotsReceived = krakenOrderBook.getData().size();
                if (numSnapshotsReceived != 1) {
                    String errMsg = "Got Kraken snapshot OrderBook that has [" + numSnapshotsReceived + "] snapshots instead of 1";
                    log.error(errMsg + ". The full json that was received: [" + json + "]");
                    throw new RuntimeException(errMsg + ".  Check the log for the full json string.");
                }
                KrakenOrderBook2Data krakenOrderBookData = krakenOrderBook.getData().get(0);
                CbOrderBook orderBook = objectConverter.convertToCbOrderBook(krakenOrderBookData);

            }
            // TODO: check
            //      checksum - if wrong, then insert into alert table (have a separate process to see if there are new alerts in the table to email about), drop the update and try to get snapshot again and start over (see how that would work)
            //              confirm that checksum for an update is to be calculated after applying the update on previous snapshot
            //      check timestamp to see if a time gap between when data is generated in Kraken and when it's processed has gotten beyond some limit, in which case insert into alert table
            // TODO: pass to BatchProcessor
        }

    }

    // TODO: check if timeOfLastHeartbeat is past some limit

}
