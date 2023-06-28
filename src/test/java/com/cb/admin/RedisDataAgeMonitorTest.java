package com.cb.admin;

import com.cb.alert.Alerter;
import com.cb.common.JsonSerializer;
import com.cb.common.util.TimeUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisDataAgeMonitorTest {

    @Mock
    private Alerter alerter;

    @Spy
    private JsonSerializer jsonSerializer;

    @InjectMocks
    private RedisDataAgeMonitor redisDataAgeMonitor;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(alerter);
    }

    @Test
    public void monitorRedisKey_RedisKeyNotExists() {
        // setup
        String redisKey = "redisKey1";
        int ageLimit_DoesNotMatter = 12;
        Instant timeToCompare_DoesNotMatter = Instant.now();
        Jedis jedis = mock(Jedis.class);
        when(jedis.exists(redisKey)).thenReturn(false);

        // engage test
        redisDataAgeMonitor.monitorRedisKey(jedis, redisKey, ageLimit_DoesNotMatter, timeToCompare_DoesNotMatter);

        // verify
        verify(jedis, times(0)).zpopmax(anyString());
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorRedisKey_ageBelowLimit() {
        // prepare data
        Jedis jedis = mock(Jedis.class);
        String redisKey = "doesn't matter";
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);
        when(jedis.exists(redisKey)).thenReturn(true);
        when(jedis.zpopmax(redisKey)).thenReturn(sampleRedisData(timeOfLastItem));

        // engage test
        redisDataAgeMonitor.monitorRedisKey(jedis, redisKey, 6, timeToCompare);

        // verify
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorRedisKey_ageAtLimit() {
        // prepare data
        Jedis jedis = mock(Jedis.class);
        String redisKey = "doesn't matter";
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);
        when(jedis.exists(redisKey)).thenReturn(true);
        when(jedis.zpopmax(redisKey)).thenReturn(sampleRedisData(timeOfLastItem));

        // engage test
        redisDataAgeMonitor.monitorRedisKey(jedis, redisKey, 5, timeToCompare);

        // verify
        verify(alerter, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorRedisKey_ageAboveLimit() {
        // prepare data
        Jedis jedis = mock(Jedis.class);
        String redisKey = "doesn't matter";
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);
        when(jedis.exists(redisKey)).thenReturn(true);
        when(jedis.zpopmax(redisKey)).thenReturn(sampleRedisData(timeOfLastItem));

        // engage test
        redisDataAgeMonitor.monitorRedisKey(jedis, redisKey, 4, timeToCompare);

        // verify
        verify(alerter, times(1)).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void checkExchangeDatetime() {
        assertDoesNotThrow(() -> redisDataAgeMonitor.checkExchangeDatetime(Instant.now(), null, null));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> redisDataAgeMonitor.checkExchangeDatetime(null, null, null));
        assertEquals("Encountered a snapshot in Redis that has null exchangeDatetime. Check logs.", exception.getMessage());
    }

    private static Tuple sampleRedisData(Instant exchangeDateTime) {
        long exchangeDateTimeMillis = exchangeDateTime.toEpochMilli();
        long exchangeDateTimeMicros = TimeUtils.micros(exchangeDateTime);
        String orderBookJson = "{\"exchangeDatetime\":" + exchangeDateTimeMillis + ",\"exchangeDate\":\"1995-04-08\",\"receivedMicros\":" + exchangeDateTimeMicros + ",\"bids\":{\"10.1\":0.5,\"10.2\":1.77,\"10.3\":0.9},\"asks\":{\"10.5\":1.89,\"10.6\":54.899,\"10.7\":21.7}}";
        return new Tuple(orderBookJson, (double)exchangeDateTimeMicros);
    }

}
