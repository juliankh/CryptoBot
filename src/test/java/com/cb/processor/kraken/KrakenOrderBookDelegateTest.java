package com.cb.processor.kraken;

import com.cb.common.util.TimeUtils;
import com.cb.processor.kraken.channel_status.KrakenChannelStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class KrakenOrderBookDelegateTest {

    @InjectMocks
    private KrakenOrderBookDelegate delegate;

    @Test
    public void orderBookAge() {
        Instant exchangeDateTime = TimeUtils.instant(2015, Month.NOVEMBER, 25, 10, 30, 39);
        Instant timeToCompareTo = TimeUtils.instant(2015, Month.NOVEMBER, 25, 10, 30, 45);
        assertNull(delegate.orderBookAge(null, timeToCompareTo));
        assertEquals(6, delegate.orderBookAge(exchangeDateTime, timeToCompareTo));
    }

    @Test
    public void orderBookStale() {
        KrakenChannelStatus channelStatus_DoesNotMatter = KrakenChannelStatus.online;

        assertFalse(delegate.orderBookStale(true, channelStatus_DoesNotMatter, 5L, 10));
        assertFalse(delegate.orderBookStale(true, channelStatus_DoesNotMatter, 10L, 10));
        assertFalse(delegate.orderBookStale(true, channelStatus_DoesNotMatter, null, 10));
        assertFalse(delegate.orderBookStale(false, channelStatus_DoesNotMatter, 11L, 10));
        assertFalse(delegate.orderBookStale(false, channelStatus_DoesNotMatter, null, 10));
        assertFalse(delegate.orderBookStale(null, channelStatus_DoesNotMatter, 11L, 10));

        assertTrue(delegate.orderBookStale(true, channelStatus_DoesNotMatter, 11L, 10));
    }

}
