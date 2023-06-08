package com.cb.processor.kraken;

import com.cb.common.CurrencyResolver;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.KrakenBatch;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBook2Data;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseOrderBook;
import com.cb.processor.BatchProcessor;
import com.cb.processor.SnapshotMaintainer;
import com.cb.ws.kraken.json_converter.KrakenJsonOrderBookObjectConverter;
import com.google.common.collect.Lists;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.knowm.xchange.currency.CurrencyPair;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.reset;

@ExtendWith(MockitoExtension.class)
public class KrakenJsonOrderBookProcessorTest {

    @Spy
    private CurrencyResolver currencyResolver;

    @Mock
    private KrakenJsonOrderBookObjectConverter jsonObjectConverter;

    @Mock
    private BatchProcessor<CbOrderBook, KrakenBatch<CbOrderBook>> batchProcessor;

    @Mock
    private SnapshotMaintainer snapshotMaintainer;

    @InjectMocks
    private KrakenJsonOrderBookProcessor processor;

    @BeforeEach
    public void beforeEachTest() {
        reset(jsonObjectConverter);
        reset(batchProcessor);
        reset(snapshotMaintainer);
    }

    @Test
    public void processSubscriptionResponse_Successful() {
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(subscriptionResponse(true)));
    }

    @Test
    public void processSubscriptionResponse_Unsuccessful() {
        KrakenSubscriptionResponseOrderBook unsuccessfulResponse = subscriptionResponse(false);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processSubscriptionResponse(unsuccessfulResponse));
        assertEquals("Error when trying to subscribe to Kraken OrderBook channel: " + unsuccessfulResponse, exception.getMessage());
    }

    private static KrakenSubscriptionResponseOrderBook subscriptionResponse(boolean successful) {
        KrakenSubscriptionResponseOrderBook response = new KrakenSubscriptionResponseOrderBook();
        response.setSuccess(successful);
        return response;
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
        RuntimeException exception1 = assertThrows(RuntimeException.class, () -> processor.processOrderBookSnapshot(new KrakenOrderBookInfo().setData(null)));
        assertEquals("Got Kraken snapshot OrderBook Info that has [0] snapshots instead of 1", exception1.getMessage());

        RuntimeException exception2 = assertThrows(RuntimeException.class, () -> processor.processOrderBookSnapshot(new KrakenOrderBookInfo().setData(Lists.newArrayList())));
        assertEquals("Got Kraken snapshot OrderBook Info that has [0] snapshots instead of 1", exception2.getMessage());

        RuntimeException exception3 = assertThrows(RuntimeException.class, () -> processor.processOrderBookSnapshot(new KrakenOrderBookInfo().setData(Lists.newArrayList(new KrakenOrderBook2Data(), new KrakenOrderBook2Data()))));
        assertEquals("Got Kraken snapshot OrderBook Info that has [2] snapshots instead of 1", exception3.getMessage());
    }

    @Test
    public void processOrderBookSnapshot_SnapshotWithUnexpectedCurrencyPair() {
        // setup
        int depth_doesNotMatter = 123;
        int batchSize_doesNotMatter = 45;
        processor.initialize(CurrencyPair.BTC_USDT, depth_doesNotMatter, batchSize_doesNotMatter);

        // engage test and verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processOrderBookSnapshot(new KrakenOrderBookInfo().setData(Lists.newArrayList(new KrakenOrderBook2Data().setSymbol("LINK/USD")))));
        assertEquals("Initial Snapshot received is expected to be of Currency Pair [BTC/USDT], but has instead [LINK/USD]", exception.getMessage());
    }

    @Test
    public void processCustom_UnknownObjectTypeParsed() {
        // setup
        Class<?> unknownClass = BigDecimal.class;

        // engage test and verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processCustom(unknownClass));
        assertEquals("Unknown object type parsed: [" + unknownClass + "]", exception.getMessage());
    }

    @Test
    public void process_ExceptionThrown() {
        // setup
        String json = "some json";
        doThrow(NullPointerException.class).when(jsonObjectConverter).parse(json);

        // engage test and verify
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.process(json));
        assertEquals("Problem processing json: [" + json + "]", exception.getMessage());
    }

}
