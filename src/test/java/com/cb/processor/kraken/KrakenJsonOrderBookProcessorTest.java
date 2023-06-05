package com.cb.processor.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.common.ObjectConverter;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.OrderBookBatch;
import com.cb.model.kraken.ws.KrakenOrderBook;
import com.cb.model.kraken.ws.KrakenOrderBook2Data;
import com.cb.processor.BatchProcessor;
import com.cb.processor.JedisDelegate;
import com.cb.processor.SnapshotMaintainer;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class KrakenJsonOrderBookProcessorTest {

    @Spy
    private CurrencyResolver currencyResolver;

    @Mock
    private ObjectConverter objectConverter;

    @Mock
    private BatchProcessor<CbOrderBook, OrderBookBatch<CbOrderBook>> batchProcessor;

    @Mock
    private SnapshotMaintainer snapshotMaintainer;

    @Mock
    private JedisDelegate jedisDelegate;

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

    @Test
    public void processOrderBookSnapshot_UnexpectedNumSnapshots() {
        assertThrows(
                "Got Kraken snapshot OrderBook that has [0] snapshots instead of 1",
                RuntimeException.class,
                () -> processor.processOrderBookSnapshot(new KrakenOrderBook().setData(null)));
        assertThrows(
                "Got Kraken snapshot OrderBook that has [0] snapshots instead of 1",
                RuntimeException.class,
                () -> processor.processOrderBookSnapshot(new KrakenOrderBook().setData(Lists.newArrayList())));
        assertThrows(
                "Got Kraken snapshot OrderBook that has [2] snapshots instead of 1",
                RuntimeException.class,
                () -> processor.processOrderBookSnapshot(new KrakenOrderBook().setData(Lists.newArrayList(new KrakenOrderBook2Data(), new KrakenOrderBook2Data()))));
    }

    @Test
    public void processOrderBookSnapshot_SnapshotWithUnexpectedCurrencyPair() {
        // setup
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.BTC_USDT, depth_doesNotMatter, batchSize_doesNotMatter);

        // engage test and verify
        assertThrows(
                "Initial Snapshot received is expected to be of Currency Pair [BTC/USDT], but has instead [LINK/USD]",
                RuntimeException.class,
                () -> processor.processOrderBookSnapshot(new KrakenOrderBook().setData(Lists.newArrayList(new KrakenOrderBook2Data().setSymbol("LINK/USD")))));
    }

}
