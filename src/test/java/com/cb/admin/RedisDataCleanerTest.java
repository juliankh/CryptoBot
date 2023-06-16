package com.cb.admin;

import com.cb.db.ReadOnlyDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import redis.clients.jedis.Jedis;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RedisDataCleanerTest {

    @Mock
    private ReadOnlyDao readOnlyDao;

    @InjectMocks
    private RedisDataCleaner redisDataCleaner;

    @BeforeEach
    public void beforeEachTest() {
        Mockito.reset(readOnlyDao);
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
