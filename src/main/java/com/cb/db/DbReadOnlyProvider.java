package com.cb.db;

import com.cb.common.util.TimeUtils;
import com.cb.injection.module.MainModule;
import com.cb.model.CbOrderBook;
import com.cb.model.config.*;
import com.cb.model.config.db.*;
import com.cb.model.kraken.db.DbKrakenOrderBook;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.handlers.BeanListHandler;
import org.apache.commons.dbutils.handlers.ScalarHandler;
import org.knowm.xchange.currency.CurrencyPair;

import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.cb.injection.BindingName.DB_READ_CONNECTION;

@Slf4j
public class DbReadOnlyProvider extends AbstractDbProvider {

    private static final BeanListHandler<DbKrakenOrderBook> BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK = new BeanListHandler<>(DbKrakenOrderBook.class);
    private static final BeanListHandler<DbDataAgeMonitorConfig> BEAN_LIST_HANDLER_DATA_AGE_MONITOR_CONFIG = new BeanListHandler<>(DbDataAgeMonitorConfig.class);
    private static final BeanListHandler<DbRedisDataAgeMonitorConfig> BEAN_LIST_HANDLER_REDIS_DATA_AGE_MONITOR_CONFIG = new BeanListHandler<>(DbRedisDataAgeMonitorConfig.class);
    private static final BeanListHandler<DbDataCleanerConfig> BEAN_LIST_HANDLER_DATA_CLEANER_CONFIG = new BeanListHandler<>(DbDataCleanerConfig.class);
    private static final BeanListHandler<DbRedisDataCleanerConfig> BEAN_LIST_HANDLER_REDIS_DATA_CLEANER_CONFIG = new BeanListHandler<>(DbRedisDataCleanerConfig.class);
    private static final BeanListHandler<DbProcessConfig> BEAN_LIST_HANDLER_PROCESS_CONFIG = new BeanListHandler<>(DbProcessConfig.class);
    private static final BeanListHandler<DbQueueMonitorConfig> BEAN_LIST_HANDLER_QUEUE_MONITOR_CONFIG = new BeanListHandler<>(DbQueueMonitorConfig.class);
    private static final BeanListHandler<DbKrakenBridgeOrderBookConfig> BEAN_LIST_HANDLER_KRAKEN_BRIDGE_ORDERBOOK_CONFIG = new BeanListHandler<>(DbKrakenBridgeOrderBookConfig.class);
    private static final BeanListHandler<DbMiscConfig> BEAN_LIST_HANDLER_MISC_CONFIG = new BeanListHandler<>(DbMiscConfig.class);

    private static final ScalarHandler<Timestamp> TIMESTAMP_SCALAR_HANDLER = new ScalarHandler<>();

    @Inject
    @Named(DB_READ_CONNECTION)
    private Connection readConnection;

    @Inject
    private QueryRunner queryRunner;

    public static void main(String[] args) {
        DbReadOnlyProvider dbReadOnlyProvider = MainModule.INJECTOR.getInstance(DbReadOnlyProvider.class);
        dbReadOnlyProvider.miscConfig().entrySet().forEach(System.out::println);
    }

    public List<CbOrderBook> krakenOrderBooks(CurrencyPair currencyPair, Instant from, Instant to) {
        List<DbKrakenOrderBook> dbOrderBooks = krakenOrderBooks(currencyPair, Timestamp.from(from), Timestamp.from(to));
        return dbOrderBooks.parallelStream().map(objectConverter::convertToDbKrakenOrderBook).toList();
    }

    public List<DbKrakenOrderBook> krakenOrderBooks(CurrencyPair currencyPair, Timestamp from, Timestamp to) {
        try {
            String tableName = krakenTableNameResolver.krakenOrderBookTable(currencyPair);
            String sql = " SELECT id, process, exchange_datetime, exchange_date, received_micros, created, highest_bid_price, highest_bid_volume, lowest_ask_price, lowest_ask_volume, bids_hash, asks_hash, bids, asks " +
                         " FROM " + tableName +
                         " WHERE exchange_datetime between ? and ?" +
                         " ORDER BY received_micros";
            return TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK, from, to), "Retrieving", tableName);
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Kraken OrderBooks for CurrencyPair [" + currencyPair + "] between [" + from + "] and [" + to + "]", e);
        }
    }

    public List<DbKrakenOrderBook> krakenOrderBooks(CurrencyPair currencyPair, Set<Long> ids) {
        try {
            String tableName = krakenTableNameResolver.krakenOrderBookTable(currencyPair);
            String questionMarkString = questionMarks(ids.size());
            Long[] idArray = ids.toArray(new Long[0]);
            String sql = " SELECT * " +
                    " FROM " + tableName +
                    " WHERE id in (" + questionMarkString + ")" +
                    " ORDER BY received_micros";
            return TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_KRAKEN_ORDERBOOK, idArray), "Retrieving", tableName);
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Kraken OrderBooks for CurrencyPair [" + currencyPair + "] and Set of Ids [" + ids.size() + " ids]", e);
        }
    }

    public List<DataAgeMonitorConfig> dataAgeMonitorConfig() {
        try {
            String tableName = "cb.config_data_age_monitor";
            String sql = "SELECT id, table_name, column_name, mins_age_limit FROM " + tableName + ";";
            List<DbDataAgeMonitorConfig> rawConfigs = TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_DATA_AGE_MONITOR_CONFIG), "Retrieving", tableName);
            return rawConfigs.parallelStream().map(objectConverter::convertToDataAgeMonitorConfig).toList();
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Data Age Monitor Config", e);
        }
    }

    public List<RedisDataAgeMonitorConfig> redisDataAgeMonitorConfig() {
        try {
            String tableName = "cb.config_redis_data_age_monitor";
            String sql = "SELECT id, redis_key, mins_age_limit FROM " + tableName + ";";
            List<DbRedisDataAgeMonitorConfig> rawConfigs = TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_REDIS_DATA_AGE_MONITOR_CONFIG), "Retrieving", tableName);
            return rawConfigs.parallelStream().map(objectConverter::convertToRedisDataAgeMonitorConfig).toList();
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Data Age Monitor Config", e);
        }
    }

    public List<DataCleanerConfig> dataCleanerConfig() {
        try {
            String tableName = "cb.config_data_cleaner";
            String sql = "SELECT id, table_name, column_name, hours_back FROM " + tableName + ";";
            List<DbDataCleanerConfig> rawConfigs = TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_DATA_CLEANER_CONFIG), "Retrieving", tableName);
            return rawConfigs.parallelStream().map(objectConverter::convertToDataCleanerConfig).toList();
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Data Cleaner Config", e);
        }
    }

    public List<RedisDataCleanerConfig> redisDataCleanerConfig() {
        try {
            String tableName = "cb.config_redis_data_cleaner";
            String sql = "SELECT id, redis_key, mins_back FROM " + tableName + ";";
            List<DbRedisDataCleanerConfig> rawConfigs = TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_REDIS_DATA_CLEANER_CONFIG), "Retrieving", tableName);
            return rawConfigs.parallelStream().map(objectConverter::convertToRedisDataCleanerConfig).toList();
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Redis Data Cleaner Config", e);
        }
    }

    public TreeMap<String, List<ProcessConfig>> activeProcessConfigMap() {
        List<ProcessConfig> activeProcessConfigs = activeProcessConfigs();
        TreeMap<String, List<ProcessConfig>> result = activeProcessConfigs.parallelStream().collect(Collectors.groupingBy(ProcessConfig::getProcessToken, TreeMap::new, Collectors.toList()));
        return result;
    }

    public List<ProcessConfig> activeProcessConfigs() {
        return processConfigs(true);
    }

    public List<ProcessConfig> processConfigs(boolean active) {
        List<ProcessConfig> safetyNets = processConfigs();
        return safetyNets.parallelStream().filter(config -> config.isActive() == active).toList();
    }

    public List<ProcessConfig> processConfigs() {
        try {
            String tableName = "cb.config_process";
            String sql = "SELECT id, process_token, process_subtoken, active FROM " + tableName + ";";
            List<DbProcessConfig> rawConfigs = TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_PROCESS_CONFIG), "Retrieving", tableName);
            return rawConfigs.parallelStream().map(objectConverter::convertToProcessConfig).toList();
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Safety Nets", e);
        }
    }

    public List<QueueMonitorConfig> queueMonitorConfig() {
        try {
            String tableName = "cb.config_queue_monitor";
            String sql = "SELECT id, queue_name, message_limit FROM " + tableName + ";";
            List<DbQueueMonitorConfig> rawConfigs = TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_QUEUE_MONITOR_CONFIG), "Retrieving", tableName);
            return rawConfigs.parallelStream().map(objectConverter::convertToQueueMonitorConfig).toList();
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Queue Monitor Config", e);
        }
    }

    public Map<CurrencyPair, KrakenBridgeOrderBookConfig> krakenBridgeOrderBookConfig() {
        try {
            String tableName = "cb.config_kraken_bridge_orderbook";
            String sql = "SELECT id, currency_base, currency_counter, batch_size, secs_timeout FROM " + tableName + ";";
            List<DbKrakenBridgeOrderBookConfig> rawConfigs = TimeUtils.runTimedCallable_CollectionOutput(() -> queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_KRAKEN_BRIDGE_ORDERBOOK_CONFIG), "Retrieving", tableName);
            return rawConfigs.parallelStream().map(objectConverter::convertToKrakenBridgeOrderBookConfig).collect(Collectors.toMap(KrakenBridgeOrderBookConfig::getCurrencyPair, c -> c));
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Kraken Bridge OrderBook Config", e);
        }
    }

    public MiscConfig miscConfig(String name) {
        return miscConfig().get(name);
    }

    public Map<String, MiscConfig> miscConfig() {
        try {
            String sql = "SELECT id, name, value FROM cb.config_misc;";
            List<DbMiscConfig> rawConfigs = queryRunner.query(readConnection, sql, BEAN_LIST_HANDLER_MISC_CONFIG);
            return rawConfigs.parallelStream().map(objectConverter::convertToMiscConfig).collect(Collectors.toMap(MiscConfig::getName, Function.identity()));
        } catch (Exception e) {
            throw new RuntimeException("Problem retrieving Misc Config", e);
        }
    }

    public Instant timeOfLastItem(String table, String column) {
        try {
            Timestamp result = queryRunner.query(readConnection, "SELECT max(" + column + ") FROM " + table + ";", TIMESTAMP_SCALAR_HANDLER);
            return result.toInstant();
        } catch (Exception e) {
            throw new RuntimeException("Problem getting the time of last item in table [" + table + "] using column [" + column + "]", e);
        }
    }

    @SneakyThrows
    public void cleanup() {
        log.info("Cleaning up");
        DbUtils.closeQuietly(readConnection);
    }

}
