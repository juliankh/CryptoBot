package com.cb.sandbox.redis;

import com.cb.common.util.NumberUtils;
import com.cb.common.util.TimeUtils;
import com.cb.db.DbReadOnlyProvider;
import com.cb.injection.module.MainModule;
import com.cb.model.CbOrderBook;
import com.cb.model.json.adapter.InstantAdapter;
import com.cb.model.json.adapter.LocalDateAdapter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.CurrencyPair;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.Jedis;

import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RedisQueryPerformanceExp {

    public static void main(String[] args) {
        System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().totalMemory()));
        System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().maxMemory()));
        System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().freeMemory()));

        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(Instant.class, new InstantAdapter());
        builder.registerTypeAdapter(LocalDate.class, new LocalDateAdapter());
        Gson gson = builder.create();

        Instant start = Instant.now();
        DbReadOnlyProvider dbReadOnlyProvider = MainModule.INJECTOR.getInstance(DbReadOnlyProvider.class);
        Instant to = Instant.now().minus(1, ChronoUnit.MINUTES);
        Instant from = to.minus(4, ChronoUnit.MINUTES);
        List<CbOrderBook> orderBooks = dbReadOnlyProvider.krakenOrderBooks(CurrencyPair.BTC_USDT, from, to);
        Instant end = Instant.now();
        double queryRate = TimeUtils.ratePerSecond(start, end, orderBooks.size());
        log.info("Retrieving and converting [" + NumberUtils.numberFormat(orderBooks.size()) + "] items took [" + TimeUtils.durationMessage(start, end) + "] at rate of [" + NumberUtils.numberFormat(queryRate) + "/sec]");

        Instant start3 = Instant.now();
        Map<String, Double> payloadMap = orderBooks.parallelStream().collect(Collectors.toMap(gson::toJson, orderbook -> (double)(orderbook.getReceivedMicros()), (a, b)->a));
        Instant end3 = Instant.now();
        double queryRate3 = TimeUtils.ratePerSecond(start3, end3, payloadMap.size());
        log.info("Converting to json [" + NumberUtils.numberFormat(payloadMap.size()) + "] items took [" + TimeUtils.durationMessage(start3, end3) + "] at rate of [" + NumberUtils.numberFormat(queryRate3) + "/sec]");
/**/
        Jedis jedis = new Jedis("localhost", 6379, DefaultJedisClientConfig.builder().connectionTimeoutMillis(600_000).socketTimeoutMillis(400_000).build());
/**/
        Instant start4 = Instant.now();
        jedis.zadd(CurrencyPair.BTC_USDT.toString(), payloadMap);
        Instant end4 = Instant.now();
        double queryRate4 = TimeUtils.ratePerSecond(start4, end4, payloadMap.size());
        log.info("Inserting into redis [" + NumberUtils.numberFormat(payloadMap.size()) + "] items took [" + TimeUtils.durationMessage(start4, end4) + "] at rate of [" + NumberUtils.numberFormat(queryRate4) + "/sec]");
/**/
        Instant start2 = Instant.now();
        List<String> result = jedis.zrange(CurrencyPair.BTC_USDT.toString(), 0, Long.MAX_VALUE);
        Instant end2 = Instant.now();
        double queryRate2 = TimeUtils.ratePerSecond(start2, end2, result.size());
        log.info("Querying Redis [" + NumberUtils.numberFormat(result.size()) + "] items took [" + TimeUtils.durationMessage(start2, end2) + "] at rate of [" + NumberUtils.numberFormat(queryRate2) + "/sec]");

        jedis.close();

        Instant start5 = Instant.now();
        List<CbOrderBook> resultOrderBooks = result.parallelStream().map(json -> gson.fromJson(json, CbOrderBook.class)).toList();
        /*
        List<CbOrderBook> resultOrderBooks = new ArrayList<>();
        List<List<String>> batches = Lists.partition(result, 10_000);
        AtomicInteger batchNum = new AtomicInteger();
        batches.forEach(batch -> {
            System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().totalMemory()));
            System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().maxMemory()));
            System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().freeMemory()));
            resultOrderBooks.addAll(batch.parallelStream().map(json -> gson.fromJson(json, CbOrderBook.class)).toList());
            System.out.println("batch " + batchNum.incrementAndGet() + " processed");
        });*/
        Instant end5 = Instant.now();
        double queryRate5 = TimeUtils.ratePerSecond(start5, end5, resultOrderBooks.size());
        log.info("Converting [" + NumberUtils.numberFormat(resultOrderBooks.size()) + "] OrderBooks from json took [" + TimeUtils.durationMessage(start5, end5) + "] at rate of [" + NumberUtils.numberFormat(queryRate5) + "/sec]");
        double queryRate6 = TimeUtils.ratePerSecond(start2, end5, resultOrderBooks.size());
        log.info("Querying and Converting [" + NumberUtils.numberFormat(resultOrderBooks.size()) + "] OrderBooks from json took [" + TimeUtils.durationMessage(start2, end5) + "] at rate of [" + NumberUtils.numberFormat(queryRate6) + "/sec]");

/*
        ConnectionString connectionString = new ConnectionString("mongodb://localhost:27017");
        CodecRegistry pojoCodecRegistry = CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build());
        CodecRegistry codecRegistry = CodecRegistries.fromRegistries(MongoClientSettings.getDefaultCodecRegistry(), pojoCodecRegistry);
        MongoClientSettings clientSettings = MongoClientSettings.builder().applyConnectionString(connectionString).codecRegistry(codecRegistry).build();
        Instant start2 = Instant.now();
        try (MongoClient mongoClient = MongoClients.create(clientSettings)) {
            MongoDatabase database = mongoClient.getDatabase("cb");
            database.getCollection("test_collection", MongoOrderBook.class).drop();
            database.createCollection("test_collection");
            MongoCollection<MongoOrderBook> collection = database.getCollection("test_collection", MongoOrderBook.class);
            collection.insertMany(orderBooks2);
        }
        Instant end2 = Instant.now();
        double queryRate2 = TimeUtils.ratePerSecond(start2, end2, orderBooks2.size());
        log.info("Inserting into MongoDb [" + NumberUtils.numberFormat(orderBooks2.size()) + "] items took [" + TimeUtils.durationMessage(start2, end2) + "] at rate of [" + NumberUtils.numberFormat(queryRate2) + "/sec]");

        Instant start3 = Instant.now();
        try (MongoClient mongoClient = MongoClients.create(clientSettings)) {
            MongoDatabase database = mongoClient.getDatabase("cb");
            MongoCollection<MongoOrderBook> collection = database.getCollection("test_collection", MongoOrderBook.class);
            FindIterable<MongoOrderBook> cursor = collection.find();
            List<MongoOrderBook> retrieved = new ArrayList<>();
            try (final MongoCursor<MongoOrderBook> cursorIterator = cursor.cursor()) {
                while (cursorIterator.hasNext()) {
                    retrieved.add(cursorIterator.next());
                }
            }
            Instant end3 = Instant.now();
            double queryRate3 = TimeUtils.ratePerSecond(start3, end3, retrieved.size());
            log.info("Querying MongoDb [" + NumberUtils.numberFormat(retrieved.size()) + "] items took [" + TimeUtils.durationMessage(start3, end3) + "] at rate of [" + NumberUtils.numberFormat(queryRate3) + "/sec]");
        }*/

        System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().totalMemory()));
        System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().maxMemory()));
        System.out.println(NumberUtils.numberFormat(Runtime.getRuntime().freeMemory()));
    }

}
