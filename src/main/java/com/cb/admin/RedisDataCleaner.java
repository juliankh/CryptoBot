package com.cb.admin;

import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.db.ReadOnlyDao;
import com.cb.injection.module.MainModule;
import com.cb.model.config.RedisDataCleanerConfig;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class RedisDataCleaner {

    @Inject
    private ReadOnlyDao readOnlyDao;

    public void prune() {
        List<RedisDataCleanerConfig> configs = readOnlyDao.redisDataCleanerConfig();
        log.info("Configs:\n\t" + configs.parallelStream().map(Object::toString).sorted().collect(Collectors.joining("\n\t")));
        configs.parallelStream().forEach(config -> {
            String redisKey = config.getRedisKey();
            int minsLimit = config.getMinsBack();
            try (Jedis jedis = MainModule.INJECTOR.getInstance(Jedis.class)) {  // Jedis is not threat-safe object, so need to create a new instance for every parallel call to Redis
                pruneRedisKey(jedis, redisKey, minsLimit);
            }
        });
    }

    public void pruneRedisKey(Jedis jedis, String redisKey, int minsLimit) {
        if (jedis.exists(redisKey)) {
            long microsMinsBack = TimeUtils.micros(Instant.now().minus(minsLimit, ChronoUnit.MINUTES));
            double scoreMin = Double.MIN_VALUE;
            double scoreMax = (double) microsMinsBack;
            log.info("Will try to prune data for Redis Key [" + redisKey + "], Score Min [" + scoreMin + "], Score Max [" + scoreMax + "]");
            long numRemoved = TimeUtils.runTimedCallable_NumberedOutput(() -> jedis.zremrangeByScore(redisKey, scoreMin, scoreMax), "Pruning", redisKey + " OrderBook");
            log.info("For Redis Key [" + redisKey + "] pruned [" + NumberUtils.numberFormat(numRemoved) + "] members which were > [" + NumberUtils.numberFormat(minsLimit) + "] mins old");
        } else {
            log.warn("Redis Key [" + redisKey + "] doesn't exist");
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        readOnlyDao.cleanup();
    }

}
