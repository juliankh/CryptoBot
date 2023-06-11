package com.cb.processor.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.common.util.GeneralUtils;
import com.cb.common.util.TimeUtils;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.KrakenBatch;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBook2Data;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseOrderBook;
import com.cb.processor.BatchProcessor;
import com.cb.processor.JedisDelegate;
import com.cb.processor.SnapshotMaintainer;
import com.cb.processor.checksum.ChecksumCalculator;
import com.cb.ws.kraken.json_converter.KrakenJsonOrderBookObjectConverter;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;

import javax.inject.Inject;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
public class KrakenJsonOrderBookProcessor extends KrakenAbstractJsonProcessor {

    private static final int SLEEP_SECS_BETWEEN_SNAPSHOT_AGE_CHECK = 10;

    @Inject
    private CurrencyResolver currencyResolver;

    @Inject
    private ObjectConverter objectConverter;

    @Inject
    private KrakenJsonOrderBookObjectConverter jsonObjectConverter;

    @Inject
    private BatchProcessor<CbOrderBook, KrakenBatch<CbOrderBook>> batchProcessor;

    @Inject
    private SnapshotMaintainer snapshotMaintainer;

    @Inject
    private JedisDelegate jedisDelegate;

    private CurrencyPair currencyPair;

    public void initialize(CurrencyPair currencyPair, int depth, int batchSize, ChecksumCalculator checksumCalculator) {
        this.currencyPair = currencyPair;
        snapshotMaintainer.initialize(depth, checksumCalculator);
        batchProcessor.initialize(batchSize);
        CompletableFuture.runAsync(() -> {
            while (true) {
                log.info(snapshotMaintainer.snapshotAgeLogMsg(Instant.now()));
                TimeUtils.sleepQuietlyForSecs(SLEEP_SECS_BETWEEN_SNAPSHOT_AGE_CHECK);
            }
        });
    }

    @Override
    public synchronized void process(String json) {
        try {
            jsonObjectConverter.parse(json);
            Class<?> objectType = jsonObjectConverter.objectTypeParsed();
            if (!processCommon(objectType, jsonObjectConverter)) {
                processCustom(objectType);
            }
        } catch (Exception e) {
            log.error("Problem processing json: [" + json + "]", e);
            throw new RuntimeException("Problem processing json: [" + GeneralUtils.truncateStringIfNecessary(json, 100) + "]", e);
        }
    }

    public void processCustom(Class<?> objectType) {
        if (objectType == KrakenSubscriptionResponseOrderBook.class) {
            processSubscriptionResponse(jsonObjectConverter.getSubscriptionResponse());
        } else if (objectType == KrakenOrderBookInfo.class) {
            processOrderBookInfo(jsonObjectConverter.getOrderBookInfo());
        } else {
            throw new RuntimeException("Unknown object type parsed: [" + objectType + "]");
        }
    }

    public void processSubscriptionResponse(KrakenSubscriptionResponseOrderBook subscriptionResponse) {
        log.info("" + subscriptionResponse);
        if (!subscriptionResponse.isSuccess()) {
            throw new RuntimeException("Error when trying to subscribe to Kraken OrderBook channel: " + subscriptionResponse);
        }
    }

    public void processOrderBookInfo(KrakenOrderBookInfo krakenOrderBookInfo) {
        if (krakenOrderBookInfo.isSnapshot()) {
            processOrderBookSnapshot(krakenOrderBookInfo);
        } else {
            processOrderBookUpdate(krakenOrderBookInfo);
        }
    }

    public void processOrderBookSnapshot(KrakenOrderBookInfo krakenOrderBookInfo) {
        int numSnapshotsReceived = Optional.ofNullable(krakenOrderBookInfo.getData()).map(List::size).orElse(0);
        if (numSnapshotsReceived != 1) {
            throw new RuntimeException("Got Kraken snapshot OrderBook Info that has [" + numSnapshotsReceived + "] snapshots instead of 1");
        }
        KrakenOrderBook2Data incomingData = krakenOrderBookInfo.getData().get(0);
        KrakenOrderBook2Data data = dataWithExpectedCurrencyPairOrNull(incomingData);
        if (data != null) {
            CbOrderBook snapshot = objectConverter.convertToCbOrderBook(data, krakenOrderBookInfo.isSnapshot());
            snapshotMaintainer.setSnapshot(snapshot);
            processSnapshot(snapshot);
        } else {
            throw new RuntimeException("Initial Snapshot received is expected to be of Currency Pair [" + currencyPair + "], but has instead [" + incomingData.getSymbol() + "]");
        }
    }

    public void processOrderBookUpdate(KrakenOrderBookInfo krakenOrderBookInfo) {
        List<KrakenOrderBook2Data> datas = datasWithExpectedCurrencyPair(krakenOrderBookInfo.getData());
        List<CbOrderBook> updates = datas.parallelStream().map(data -> objectConverter.convertToCbOrderBook(data, krakenOrderBookInfo.isSnapshot())).toList();
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
                (List<CbOrderBook> orderbooks) -> new KrakenBatch<>(currencyPair, orderbooks),
                jedisDelegate::insertBatch);
    }

    @Override
    public void cleanup() {
        log.info("Cleaning up");
        super.cleanup();
        jedisDelegate.cleanup();
    }

}
