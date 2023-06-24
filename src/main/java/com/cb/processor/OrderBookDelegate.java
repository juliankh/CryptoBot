package com.cb.processor;

import com.cb.common.util.TimeUtils;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.function.Supplier;

@Slf4j
public class OrderBookDelegate {

    private static final int SLEEP_SECS_BETWEEN_SNAPSHOT_AGE_CHECK = 10;

    public void engageLatestOrderBookAgeMonitor(Supplier<Instant> exchangeDateTimeSupplier) {
        TimeUtils.loopForeverAsync(() -> latestOrderBookAgeMonitorIteration(exchangeDateTimeSupplier), SLEEP_SECS_BETWEEN_SNAPSHOT_AGE_CHECK);
    }

    // TODO: unit test
    public void latestOrderBookAgeMonitorIteration(Supplier<Instant> exchangeDateTimeSupplier) {
        try {
            Instant latestSnapshotExchangeDateTime = exchangeDateTimeSupplier.get();
            checkOrderBookAge(latestSnapshotExchangeDateTime, Instant.now());
        } catch (Exception e) {
            log.error("Problem while checking for the age of the latest OrderBook snapshot", e);
            throw e;
        }
    }

    public void checkOrderBookAge(Instant exchangeDateTime, Instant timeToCompareTo) {
        if (exchangeDateTime == null) {
            log.info("Latest OrderBook Snapshot and/or ExchangeDateTime hasn't been set yet");
            return;
        }
        long ageInSecs = ChronoUnit.SECONDS.between(exchangeDateTime, timeToCompareTo);
        log.info("Latest OrderBook Snapshot was generated [" + ageInSecs + "] secs ago within the exchange");
    }

}
