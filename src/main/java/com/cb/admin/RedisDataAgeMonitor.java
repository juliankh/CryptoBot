package com.cb.admin;

import com.cb.alert.Alerter;
import com.cb.common.JsonSerializer;
import com.cb.db.ReadOnlyDao;
import com.cb.injection.module.MainModule;
import com.cb.model.CbOrderBook;
import com.cb.model.config.RedisDataAgeMonitorConfig;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class RedisDataAgeMonitor {

    @Inject
    private ReadOnlyDao readOnlyDao;

    @Inject
    private JsonSerializer jsonSerializer;

    @Inject
    private Alerter alerter;

    public void monitor() {
        List<RedisDataAgeMonitorConfig> configs = readOnlyDao.redisDataAgeMonitorConfig();
        log.info("Configs:\n\t" + configs.parallelStream().map(Object::toString).sorted().collect(Collectors.joining("\n\t")));
        configs.parallelStream().forEach(config -> {
            String redisKey = config.getRedisKey();
            int ageLimit = config.getMinsAgeLimit();
            Instant timeToCompare = Instant.now();
            try (Jedis jedis = MainModule.INJECTOR.getInstance(Jedis.class)) {  // Jedis is not threat-safe object, so need to create a new instance for every parallel call to Redis
                monitorRedisKey(jedis, redisKey, ageLimit, timeToCompare);
            }
        });
    }

    @SneakyThrows
    public void monitorRedisKey(Jedis jedis, String redisKey, int ageLimit, Instant timeToCompare) {
        if (jedis.exists(redisKey)) {
            Tuple oldest = jedis.zpopmax(redisKey);
            String jsonOldest = oldest.getElement();
            CbOrderBook oldestOrderBook = jsonSerializer.deserializeFromJson(jsonOldest, CbOrderBook.class);


            Instant exchangeDatetime = oldestOrderBook.getExchangeDatetime();
            long minsAge = ChronoUnit.MINUTES.between(exchangeDatetime, timeToCompare);
            if (minsAge > ageLimit) {
                String msg = "For RedisKey [" + redisKey + "] the last item is [" + minsAge + "] mins old, which is > limit of [" + ageLimit + "] mins";
                log.warn(msg);
                alerter.sendEmailAlert(msg, msg);
            } else {
                log.info("For RedisKey [" + redisKey + "] the last item is [" + minsAge + "] mins old, which is within limit of [" + ageLimit + "] mins");
            }
        } else {
            log.warn("Redis Key [" + redisKey + "] doesn't exist");
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        readOnlyDao.cleanup();
    }

}
