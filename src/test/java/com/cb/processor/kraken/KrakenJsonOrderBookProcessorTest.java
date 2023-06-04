package com.cb.processor.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.OrderBookBatch;
import com.cb.model.kraken.ws.KrakenOrderBook2Data;
import com.cb.processor.BatchProcessor;
import com.cb.processor.SnapshotMaintainer;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.reset;

@RunWith(MockitoJUnitRunner.class)
public class KrakenJsonOrderBookProcessorTest {

    @Spy
    private CurrencyResolver currencyResolver;

    @Mock
    private BatchProcessor<CbOrderBook, OrderBookBatch<CbOrderBook>> batchProcessor;

    @Mock
    private SnapshotMaintainer snapshotMaintainer;

    @InjectMocks
    private KrakenJsonOrderBookProcessor processor;

    @Before
    public void beforeEachTest() {
        reset(batchProcessor);
        reset(snapshotMaintainer);
    }

    @Test
    public void hasExpectedCurrencyPair_Yes() {
        // setup
        KrakenOrderBook2Data data = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.BTC_USDT, depth_doesNotMatter, batchSize_doesNotMatter);

        // engage test
        assertTrue(processor.hasExpectedCurrencyPair(data));
    }

    @Test
    public void hasExpectedCurrencyPair_No() {
        // setup
        KrakenOrderBook2Data data = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.LINK_USD, depth_doesNotMatter, batchSize_doesNotMatter);

        // engage test
        assertFalse(processor.hasExpectedCurrencyPair(data));
    }

    /*
    // TODO: unit test
    public List<KrakenOrderBook2Data> datasWithExpectedCurrencyPair(List<KrakenOrderBook2Data> datas) {
        return datas.stream().map(this::dataWithExpectedCurrencyPairOrNull).filter(Objects::nonNull).toList();
    }
     */
    @Test
    public void datasWithExpectedCurrencyPair_SomeMatch() {
        // setup
        KrakenOrderBook2Data data1 = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        KrakenOrderBook2Data data2 = new KrakenOrderBook2Data().setSymbol("LINK/USD");
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.LINK_USD, depth_doesNotMatter, batchSize_doesNotMatter);
        List<KrakenOrderBook2Data> datas = Lists.newArrayList(data1, data2);

        // engage test
        List<KrakenOrderBook2Data> result = processor.datasWithExpectedCurrencyPair(datas);

        // verify
        assertEquals(1, result.size());
        assertSame(data2, result.get(0));
    }

    @Test
    public void datasWithExpectedCurrencyPair_NoneMatch() {
        // setup
        KrakenOrderBook2Data data1 = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        KrakenOrderBook2Data data2 = new KrakenOrderBook2Data().setSymbol("LINK/USD");
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.ADA_BTC, depth_doesNotMatter, batchSize_doesNotMatter);
        List<KrakenOrderBook2Data> datas = Lists.newArrayList(data1, data2);

        // engage test
        List<KrakenOrderBook2Data> result = processor.datasWithExpectedCurrencyPair(datas);

        // verify
        assertEquals(Collections.emptyList(), result);
    }

    @Test
    public void dataWithExpectedCurrencyPairOrNull_Null() {
        // setup
        KrakenOrderBook2Data data = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.LINK_USD, depth_doesNotMatter, batchSize_doesNotMatter);

        // engage test
        KrakenOrderBook2Data result = processor.dataWithExpectedCurrencyPairOrNull(data);

        // verify
        assertNull(result);
    }

    @Test
    public void dataWithExpectedCurrencyPairOrNull_NonNull() {
        // setup
        KrakenOrderBook2Data data = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.BTC_USDT, depth_doesNotMatter, batchSize_doesNotMatter);

        // engage test
        KrakenOrderBook2Data result = processor.dataWithExpectedCurrencyPairOrNull(data);

        // verify
        assertNotNull(result);
        assertSame(data, result);
    }

    /*
    // TODO: unit test
    public void processOrderBookSnapshot(KrakenOrderBook krakenOrderBook) {
        int numSnapshotsReceived = krakenOrderBook.getData().size();
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
     */
    @Test
    public void processOrderBookSnapshot() {
        // TODO: continue here
    }

}
