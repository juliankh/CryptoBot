package com.cb.processor.kraken;

import com.cb.processor.kraken.channel_status.KrakenChannelStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;

import javax.inject.Singleton;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.function.Supplier;

@Slf4j
@Singleton
public class KrakenOrderBookDelegate {

    public boolean orderBookStale(Supplier<Instant> exchangeDateTimeSupplier, Supplier<Instant> latestReceivedSupplier, Supplier<KrakenChannelStatus> channelStatusSupplier, int ageLimitSecs, Instant timeToCompareTo) {
        Long orderBookAgeSecs = age(exchangeDateTimeSupplier.get(), timeToCompareTo);
        Long latestReceivedAgeSecs = age(latestReceivedSupplier.get(), timeToCompareTo); // no logic is based on it, only logged in order to compare to orderBookAgeSecs if needed
        KrakenChannelStatus channelStatus = channelStatusSupplier.get();
        Boolean dataStreamExpected = Optional.ofNullable(channelStatus).map(KrakenChannelStatus::isDataStreamExpected).orElse(null);
        log.info("Channel status [" + channelStatus + "], data stream expected [" + dataStreamExpected + "], age limit [" + ageLimitSecs + "], age of latest order book in secs as per exchange [" + ageString(orderBookAgeSecs) + "], age of latest physically received order book data [" + ageString(latestReceivedAgeSecs) + "]");
        return orderBookStale(dataStreamExpected, channelStatus, orderBookAgeSecs, ageLimitSecs);
    }

    public String ageString(Long ageSecs) {
        return ageSecs == null ? "not set yet" : "" + ageSecs;
    }

    public Long age(Instant instant, Instant timeToCompareTo) {
        return instant == null ? null : ChronoUnit.SECONDS.between(instant, timeToCompareTo);
    }

    public boolean orderBookStale(Boolean dataStreamExpected, KrakenChannelStatus channelStatus, Long orderBookAge, long ageLimit) {
        if (BooleanUtils.isTrue(dataStreamExpected) && orderBookAge != null && orderBookAge > ageLimit) {
            log.error("The age of latest orderbook in secs [" + orderBookAge + "] is > limit [" + ageLimit + "] (channel status [" + channelStatus + "])");
            return true;
        }
        return false;
    }

}
