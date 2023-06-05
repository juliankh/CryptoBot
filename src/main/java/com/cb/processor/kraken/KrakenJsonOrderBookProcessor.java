package com.cb.processor.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbReadOnlyProvider;
import com.cb.db.DbWriteProvider;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.OrderBookBatch;
import com.cb.model.kraken.ws.*;
import com.cb.processor.BatchProcessor;
import com.cb.processor.JedisDelegate;
import com.cb.processor.JsonProcessor;
import com.cb.processor.SnapshotMaintainer;
import com.cb.ws.kraken.KrakenJsonToObjectConverter;
import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class KrakenJsonOrderBookProcessor implements JsonProcessor {

    private static final int SLEEP_SECS_BETWEEN_SNAPSHOT_AGE_CHECK = 10;

    private Instant timeOfLastHeartbeat = Instant.now();

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private KrakenJsonToObjectConverter krakenJsonToObjectConverter;

    @Inject
    private DbWriteProvider dbWriteProvider;

    @Inject
    private BatchProcessor<CbOrderBook, OrderBookBatch<CbOrderBook>> batchProcessor;

    @Inject
    private SnapshotMaintainer snapshotMaintainer;

    @Inject
    private JedisDelegate jedisDelegate;

    private CurrencyPair currencyPair;

    public void initialize(CurrencyPair currencyPair, int depth, int batchSize) {
        this.currencyPair = currencyPair;
        snapshotMaintainer.initialize(depth);
        batchProcessor.initialize(batchSize);
        CompletableFuture.runAsync(() -> {
            while (true) {
                log.info(snapshotMaintainer.snapshotAgeLogMsg(Instant.now())); // TODO: manually verify
                TimeUtils.sleepQuietlyForMins(SLEEP_SECS_BETWEEN_SNAPSHOT_AGE_CHECK);
            }
        });
    }

    // TODO: unit test
    @Override
    public synchronized void process(String json) {
        try {
            krakenJsonToObjectConverter.parseJson(json);
            Class<?> objectType = krakenJsonToObjectConverter.objectTypeParsed();
            if (objectType == KrakenStatusUpdate.class) {
                KrakenStatusUpdate statusUpdate = krakenJsonToObjectConverter.getStatusUpdate();
                int numDatas = Optional.ofNullable(statusUpdate.getData()).map(List::size).orElse(0);
                log.info("Status Update with [" + numDatas + "] datas: " + statusUpdate);
                dbWriteProvider.insertKrakenStatusUpdate(statusUpdate);
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
                processOrderBook(krakenJsonToObjectConverter.getOrderBook());
            }
        } catch (Exception e) {
            log.error("Problem processing json: [" + json + "]", e);
            throw e;
        }
    }

    public void processOrderBook(KrakenOrderBook krakenOrderBook) {
        if (krakenOrderBook.isSnapshot()) {
            processOrderBookSnapshot(krakenOrderBook);
        } else {
            processOrderBookUpdate(krakenOrderBook);
        }
    }

    public void processOrderBookSnapshot(KrakenOrderBook krakenOrderBook) {
        int numSnapshotsReceived = Optional.ofNullable(krakenOrderBook.getData()).map(List::size).orElse(0);
        if (numSnapshotsReceived != 1) {
            throw new RuntimeException("Got Kraken snapshot OrderBook that has [" + numSnapshotsReceived + "] snapshots instead of 1");
        }
        KrakenOrderBook2Data incomingData = krakenOrderBook.getData().get(0);
        KrakenOrderBook2Data data = dataWithExpectedCurrencyPairOrNull(incomingData);
        if (data != null) {
            CbOrderBook snapshot = objectConverter.convertToCbOrderBook(data, krakenOrderBook.isSnapshot());
            snapshotMaintainer.setSnapshot(snapshot);
            processSnapshot(snapshot);
        } else {
            throw new RuntimeException("Initial Snapshot received is expected to be of Currency Pair [" + currencyPair + "], but has instead [" + incomingData.getSymbol() + "]");
        }
    }

    public void processOrderBookUpdate(KrakenOrderBook krakenOrderBook) {
        List<KrakenOrderBook2Data> datas = datasWithExpectedCurrencyPair(krakenOrderBook.getData());
        List<CbOrderBook> updates = datas.parallelStream().map(data -> objectConverter.convertToCbOrderBook(data, krakenOrderBook.isSnapshot())).toList();
        List<CbOrderBook> snapshots = snapshotMaintainer.updateAndGetLatestSnapshots(updates, true);
        processSnapshots(snapshots);
    }

    public List<KrakenOrderBook2Data> datasWithExpectedCurrencyPair(List<KrakenOrderBook2Data> datas) {
        return datas.stream().map(this::dataWithExpectedCurrencyPairOrNull).filter(Objects::nonNull).toList();
    }

    public KrakenOrderBook2Data dataWithExpectedCurrencyPairOrNull(KrakenOrderBook2Data data) {
        if (hasExpectedCurrencyPair(data)) {
            return data;
        }
        log.warn("OrderBook data received is expected to be of Currency Pair [" + currencyPair + "], but has instead [" + data.getSymbol() + "]");
        return null;
    }

    public boolean hasExpectedCurrencyPair(KrakenOrderBook2Data data) {
        CurrencyPair dataCurrencyPair = currencyResolver.krakenCurrencyPair(data.getSymbol());
        return currencyPair.equals(dataCurrencyPair);
    }

    private void processSnapshots(List<CbOrderBook> snapshots) {
        snapshots.parallelStream().forEach(this::processSnapshot);
    }

    private void processSnapshot(CbOrderBook snapshot) {
        batchProcessor.process(
                snapshot,
                (List<CbOrderBook> orderbooks) -> new OrderBookBatch<>(currencyPair, orderbooks),
                jedisDelegate::insertBatch);
    }

    @Override
    public void cleanup() {
        log.info("Cleaning up");
        jedisDelegate.cleanup();
    }

}
