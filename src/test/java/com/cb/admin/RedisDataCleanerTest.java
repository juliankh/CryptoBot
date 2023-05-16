package com.cb.admin;

import com.cb.db.DbReadOnlyProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import redis.clients.jedis.Jedis;

import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class RedisDataCleanerTest {

    @Mock
    private DbReadOnlyProvider dbReadOnlyProvider;

    @InjectMocks
    private RedisDataCleaner redisDataCleaner;

    @Before
    public void beforeEachTest() {
        Mockito.reset(dbReadOnlyProvider);
    }

    @Test
    public void pruneRedisKey_RedisKeyExists() {
        // setup
        String redisKey = "redisKey1";
        int minsLimitDoesNotMatter = 10;
        Jedis jedis = mock(Jedis.class);
        when(jedis.exists(redisKey)).thenReturn(true);

        // engage test
        redisDataCleaner.pruneRedisKey(jedis, redisKey, minsLimitDoesNotMatter);

        // verify
        verify(jedis, times(1)).zremrangeByScore(anyString(), anyDouble(), anyDouble());
    }

    @Test
    public void pruneRedisKey_RedisKeyNotExists() {
        // setup
        String redisKey = "redisKey1";
        int minsLimitDoesNotMatter = 10;
        Jedis jedis = mock(Jedis.class);
        when(jedis.exists(redisKey)).thenReturn(false);

        // engage test
        redisDataCleaner.pruneRedisKey(jedis, redisKey, minsLimitDoesNotMatter);

        // verify
        verify(jedis, times(0)).zremrangeByScore(anyString(), anyDouble(), anyDouble());
    }

}
