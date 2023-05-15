package com.cb.admin;

import com.cb.alert.AlertProvider;
import com.cb.db.DbReadOnlyProvider;
import com.cb.injection.module.MainModule;
import com.cb.model.CbOrderBook;
import com.cb.model.config.RedisDataAgeMonitorConfig;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.resps.Tuple;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Singleton
public class DataAgeMonitor {

    @Inject
    private DbReadOnlyProvider dbProvider;

    @Inject
    private Gson gson;

    @Inject
    private AlertProvider alertProvider;

    public void monitor() {
        List<RedisDataAgeMonitorConfig> configs = dbProvider.retrieveRedisDataAgeMonitorConfig();
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

    public void monitorRedisKey(Jedis jedis, String redisKey, int ageLimit, Instant timeToCompare) {
        Tuple oldest = jedis.zpopmax(redisKey);
        String jsonOldest = oldest.getElement();
        CbOrderBook oldestOrderBook = gson.fromJson(jsonOldest, CbOrderBook.class);
        Instant exchangeDatetime = oldestOrderBook.getExchangeDatetime();
        long minsAge = ChronoUnit.MINUTES.between(exchangeDatetime, timeToCompare);
        if (minsAge > ageLimit) {
            String msg = "For RedisKey [" + redisKey + "] the last item is [" + minsAge + "] mins old, which is > limit of [" + ageLimit + "] mins";
            log.warn(msg);
            alertProvider.sendEmailAlert(msg, msg);
        } else {
            log.info("For RedisKey [" + redisKey + "] the last item is [" + minsAge + "] mins old, which is within limit of [" + ageLimit + "] mins");
        }
    }

    public void cleanup() {
        log.info("Cleaning up");
        dbProvider.cleanup();
    }

}
