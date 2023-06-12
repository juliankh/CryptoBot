package com.cb.processor.kraken;

import com.cb.common.util.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.Month;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class XchangeKrakenOrderBookBridgeProcessorTest {

    @Mock
    private AtomicReference<OrderBook> latestOrderBook;

    @InjectMocks
    private XchangeKrakenOrderBookBridgeProcessor processor;

    @BeforeEach
    public void beforeEachTest() {
        reset(latestOrderBook);
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNull() {
        // setup
        when(latestOrderBook.get()).thenReturn(null);

        // engage test and verify
        assertNull(processor.latestOrderBookExchangeDateTimeSupplier().get());
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNotNull_ExchangeDateTimeNull() {
        // setup
        OrderBook orderBook = mock(OrderBook.class);
        when(orderBook.getTimeStamp()).thenReturn(null);
        when(latestOrderBook.get()).thenReturn(orderBook);

        // engage test and verify
        assertNull(processor.latestOrderBookExchangeDateTimeSupplier().get());
    }

    @Test
    public void latestOrderBookExchangeDateTimeSupplier_SnapshotNotNull_ExchangeDateTimeNotNull() {
        // setup
        Instant exchangeDateTime = TimeUtils.instant(1999, Month.MARCH, 27, 10, 37, 15);
        OrderBook orderBook = mock(OrderBook.class);
        when(orderBook.getTimeStamp()).thenReturn(Date.from(exchangeDateTime));
        when(latestOrderBook.get()).thenReturn(orderBook);

        // engage test and verify
        assertEquals(exchangeDateTime, processor.latestOrderBookExchangeDateTimeSupplier().get());
    }

}
