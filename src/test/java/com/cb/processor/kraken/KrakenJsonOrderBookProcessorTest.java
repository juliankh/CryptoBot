package com.cb.processor.kraken;

import com.cb.alert.Alerter;
import com.cb.common.BatchProcessor;
import com.cb.common.CurrencyResolver;
import com.cb.common.util.TimeUtils;
import com.cb.model.CbOrderBook;
import com.cb.model.kraken.KrakenBatch;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBook2Data;
import com.cb.model.kraken.ws.response.orderbook.KrakenOrderBookInfo;
import com.cb.model.kraken.ws.response.subscription.KrakenSubscriptionResponseOrderBook;
import com.cb.processor.OrderBookDelegate;
import com.cb.processor.SnapshotMaintainer;
import com.cb.processor.checksum.ChecksumCalculator;
import com.cb.ws.kraken.KrakenJsonOrderBookObjectConverter;
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
import java.time.Instant;
import java.time.Month;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KrakenJsonOrderBookProcessorTest {

    private static final int DEPTH_DOES_NOT_MATTER = 123;
    private static final int BATCH_SIZE_DOES_NOT_MATTER = 45;

    private static final int REQUEST_ID = 102938;
    private static final int REQUEST_ID_DIFFERENT = 876543345;

    @Mock
    private Alerter alerter;

    @Spy
    private CurrencyResolver currencyResolver;

    @Mock
    private KrakenJsonOrderBookObjectConverter jsonObjectConverter;

    @Mock
    private BatchProcessor<CbOrderBook, KrakenBatch<CbOrderBook>> batchProcessor;

    @Mock
    private SnapshotMaintainer snapshotMaintainer;

    @Mock
    private OrderBookDelegate orderBookDelegate;

    @Mock
    private ChecksumCalculator checksumCalculator;

    @InjectMocks
    private KrakenJsonOrderBookProcessor processor;

    @BeforeEach
    public void beforeEachTest() {
        reset(alerter);
        reset(jsonObjectConverter);
        reset(batchProcessor);
        reset(snapshotMaintainer);
        reset(orderBookDelegate);
        reset(checksumCalculator);
        processor.initialize(REQUEST_ID);
    }

    @Test
    public void processSubscriptionResponse_Successful_RequestIdMatches() {
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(subscriptionResponse(true, REQUEST_ID)));
    }

    @Test
    public void processSubscriptionResponse_Successful_RequestIdDoesNotMatch() {
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(subscriptionResponse(true, REQUEST_ID_DIFFERENT)));
    }

    @Test
    public void processSubscriptionResponse_Unsuccessful_RequestIdMatches() {
        KrakenSubscriptionResponseOrderBook unsuccessfulResponse = subscriptionResponse(false, REQUEST_ID);
        RuntimeException exception = assertThrows(RuntimeException.class, () -> processor.processSubscriptionResponse(unsuccessfulResponse));
        assertEquals("Error when trying to subscribe to Kraken OrderBook channel: " + unsuccessfulResponse, exception.getMessage());
    }

    @Test
    public void processSubscriptionResponse_Unsuccessful_RequestIdDoesNotMatch() {
        KrakenSubscriptionResponseOrderBook unsuccessfulResponse = subscriptionResponse(false, REQUEST_ID_DIFFERENT);
        assertDoesNotThrow(() -> processor.processSubscriptionResponse(unsuccessfulResponse));
    }

    private static KrakenSubscriptionResponseOrderBook subscriptionResponse(boolean successful, int requestId) {
        KrakenSubscriptionResponseOrderBook response = new KrakenSubscriptionResponseOrderBook();
        response.setSuccess(successful);
        response.setReq_id(requestId);
        return response;
    }

    @Test
    public void hasExpectedCurrencyPair_Yes() {
        // setup
        KrakenOrderBook2Data data = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        processor.initialize(REQUEST_ID, CurrencyPair.BTC_USDT, DEPTH_DOES_NOT_MATTER, BATCH_SIZE_DOES_NOT_MATTER, checksumCalculator);

        // engage test
        assertTrue(processor.hasExpectedCurrencyPair(data));
    }

    @Test
    public void hasExpectedCurrencyPair_No() {
        // setup
        KrakenOrderBook2Data data = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        processor.initialize(REQUEST_ID, CurrencyPair.LINK_USD, DEPTH_DOES_NOT_MATTER, BATCH_SIZE_DOES_NOT_MATTER, checksumCalculator);

        // engage test
        assertFalse(processor.hasExpectedCurrencyPair(data));
    }

    @Test
    public void datasWithExpectedCurrencyPair_SomeMatch() {
        // setup
        KrakenOrderBook2Data data1 = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        KrakenOrderBook2Data data2 = new KrakenOrderBook2Data().setSymbol("LINK/USD");
        processor.initialize(REQUEST_ID, CurrencyPair.LINK_USD, DEPTH_DOES_NOT_MATTER, BATCH_SIZE_DOES_NOT_MATTER, checksumCalculator);
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
        processor.initialize(REQUEST_ID, CurrencyPair.ADA_BTC, DEPTH_DOES_NOT_MATTER, BATCH_SIZE_DOES_NOT_MATTER, checksumCalculator);
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
        processor.initialize(REQUEST_ID, CurrencyPair.LINK_USD, DEPTH_DOES_NOT_MATTER, BATCH_SIZE_DOES_NOT_MATTER, checksumCalculator);

        // engage test
        KrakenOrderBook2Data result = processor.dataWithExpectedCurrencyPairOrNull(data);

        // verify
        assertNull(result);
    }

    @Test
    public void dataWithExpectedCurrencyPairOrNull_NonNull() {
        // setup
        KrakenOrderBook2Data data = new KrakenOrderBook2Data().setSymbol("BTC/USDT");
        processor.initialize(REQUEST_ID, CurrencyPair.BTC_USDT, DEPTH_DOES_NOT_MATTER, BATCH_SIZE_DOES_NOT_MATTER, checksumCalculator);

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
        processor.initialize(REQUEST_ID, CurrencyPair.BTC_USDT, DEPTH_DOES_NOT_MATTER, BATCH_SIZE_DOES_NOT_MATTER, checksumCalculator);

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
        String json = "some json that can't be parsed";
        NullPointerException exception = new NullPointerException();
        doThrow(exception).when(jsonObjectConverter).parse(json);

        // engage test
        processor.process(json);

        // verify
        verify(alerter, times(1)).sendEmailAlertQuietly("Problem processing json", json, exception);
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNull() {
        // setup
        when(snapshotMaintainer.getSnapshot()).thenReturn(null);

        // engage test and verify
        assertNull(processor.latestOrderBookExchangeDateTimeSupplier().get());
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNotNull_ExchangeDateTimeNull() {
        // setup
        when(snapshotMaintainer.getSnapshot()).thenReturn(new CbOrderBook().setExchangeDatetime(null));

        // engage test and verify
        assertNull(processor.latestOrderBookExchangeDateTimeSupplier().get());
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNotNull_ExchangeDateTimeNotNull() {
        // setup
        Instant exchangeDateTime = TimeUtils.instant(1999, Month.MARCH, 27, 10, 37, 15);
        when(snapshotMaintainer.getSnapshot()).thenReturn(new CbOrderBook().setExchangeDatetime(exchangeDateTime));

        // engage test and verify
        assertEquals(exchangeDateTime, processor.latestOrderBookExchangeDateTimeSupplier().get());
    }

}
