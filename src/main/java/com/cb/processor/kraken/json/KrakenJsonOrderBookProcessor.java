package com.cb.processor.kraken.json;

import com.cb.common.BatchProcessor;
import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.KrakenBatch;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBook2Data;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseOrderBook;
import com.cb.processor.JedisDelegate;
import com.cb.processor.SnapshotMaintainer;
import com.cb.processor.checksum.ChecksumCalculator;
import com.cb.ws.kraken.KrakenJsonOrderBookObjectConverter;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;

import javax.inject.Inject;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class KrakenJsonOrderBookProcessor extends KrakenAbstractJsonProcessor {

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

    public void initialize(String driverName, int requestId, CurrencyPair currencyPair, int depth, int batchSize, ChecksumCalculator checksumCalculator) {
        super.initialize(driverName, requestId);
        this.currencyPair = currencyPair;
        snapshotMaintainer.initialize(depth, checksumCalculator);
        batchProcessor.initialize(batchSize);
    }

    @Override
    public synchronized void process(String json) {
        super.process(json, jsonObjectConverter);
    }

    @Override
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
        boolean requestIdMatches = requestIdMatches(subscriptionResponse.getReq_id());
        if (!requestIdMatches) {
            log.info("Got Subscription Response where Request ID returned [" + subscriptionResponse.getReq_id() + "] does not equal the original Request ID [" + requestId + "], so will log the response but otherwise ignore: " + subscriptionResponse);
        }
        if (requestIdMatches && !subscriptionResponse.isSuccess()) {
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
        log.info("Received OrderBook Snapshot");
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

    public CbOrderBook latestOrderBookSnapshot() {
        return snapshotMaintainer.getSnapshot();
    }

    @Override
    public void cleanup() {
        log.info("Cleaning up");
        super.cleanup();
        jedisDelegate.cleanup();
    }

}
