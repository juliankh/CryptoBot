package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.common.util.TimeUtils;
import com.cb.injection.module.MainModule;
import com.google.gson.Gson;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RedisDataAgeMonitorTest {

    @Spy
    private Gson gson = MainModule.INJECTOR.getInstance(Gson.class);

    @Mock
    private AlertProvider alertProvider;

    @InjectMocks
    private RedisDataAgeMonitor redisDataAgeMonitor;

    @Before
    public void beforeEachTest() {
        Mockito.reset(alertProvider);
    }

    @Test
    public void monitorRedisKey_ageBelowLimit() {
        // prepare data
        Jedis jedis = mock(Jedis.class);
        String redisKey = "doesn't matter";
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);
        when(jedis.zpopmax(redisKey)).thenReturn(sampleRedisData(timeOfLastItem));

        // engage test
        redisDataAgeMonitor.monitorRedisKey(jedis, redisKey, 6, timeToCompare);

        // verify
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorRedisKey_ageAtLimit() {
        // prepare data
        Jedis jedis = mock(Jedis.class);
        String redisKey = "doesn't matter";
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);
        when(jedis.zpopmax(redisKey)).thenReturn(sampleRedisData(timeOfLastItem));

        // engage test
        redisDataAgeMonitor.monitorRedisKey(jedis, redisKey, 5, timeToCompare);

        // verify
        verify(alertProvider, never()).sendEmailAlert(anyString(), anyString());
    }

    @Test
    public void monitorRedisKey_ageAboveLimit() {
        // prepare data
        Jedis jedis = mock(Jedis.class);
        String redisKey = "doesn't matter";
        Instant timeToCompare = Instant.now();
        Instant timeOfLastItem = timeToCompare.minus(5, ChronoUnit.MINUTES);
        when(jedis.zpopmax(redisKey)).thenReturn(sampleRedisData(timeOfLastItem));

        // engage test
        redisDataAgeMonitor.monitorRedisKey(jedis, redisKey, 4, timeToCompare);

        // verify
        verify(alertProvider, times(1)).sendEmailAlert(anyString(), anyString());
    }

    private static Tuple sampleRedisData(Instant exchangeDateTime) {
        long exchangeDateTimeMillis = exchangeDateTime.toEpochMilli();
        long exchangeDateTimeMicros = TimeUtils.micros(exchangeDateTime);
        String orderBookJson = "{\"exchangeDatetime\":" + exchangeDateTimeMillis + ",\"exchangeDate\":\"1995-04-08\",\"receivedMicros\":" + exchangeDateTimeMicros + ",\"bids\":{\"10.1\":0.5,\"10.2\":1.77,\"10.3\":0.9},\"asks\":{\"10.5\":1.89,\"10.6\":54.899,\"10.7\":21.7}}";
        return new Tuple(orderBookJson, (double)exchangeDateTimeMicros);
    }

}
