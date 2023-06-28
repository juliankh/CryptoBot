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

    public boolean orderBookStale(Supplier<Instant> exchangeDateTimeSupplier, Supplier<KrakenChannelStatus> channelStatusSupplier, int ageLimitSecs, Instant timeToCompareTo) {
        Long orderBookAgeSecs = orderBookAge(exchangeDateTimeSupplier.get(), timeToCompareTo);
        KrakenChannelStatus channelStatus = channelStatusSupplier.get();
        Boolean dataStreamExpected = Optional.ofNullable(channelStatus).map(KrakenChannelStatus::isDataStreamExpected).orElse(null);
        log.info("Channel status [" + channelStatus + "], data stream expected [" + dataStreamExpected + "], age limit [" + ageLimitSecs + "], age of latest order book in secs [" + (orderBookAgeSecs == null ? "not set yet" : orderBookAgeSecs) + "]");
        return orderBookStale(dataStreamExpected, channelStatus, orderBookAgeSecs, ageLimitSecs);
    }

    public Long orderBookAge(Instant exchangeDateTime, Instant timeToCompareTo) {
        return exchangeDateTime == null ? null : ChronoUnit.SECONDS.between(exchangeDateTime, timeToCompareTo);
    }

    public boolean orderBookStale(Boolean dataStreamExpected, KrakenChannelStatus channelStatus, Long orderBookAge, long ageLimit) {
        if (BooleanUtils.isTrue(dataStreamExpected) && orderBookAge != null && orderBookAge > ageLimit) {
            log.error("The age of latest orderbook in secs [" + orderBookAge + "] is > limit [" + ageLimit + "] (channel status [" + channelStatus + "])");
            return true;
        }
        return false;
    }

}
