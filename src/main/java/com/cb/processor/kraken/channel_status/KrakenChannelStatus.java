package com.cb.processor.kraken.channel_status;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum KrakenChannelStatus {

    online(true),
    post_only(true),
    cancel_only(false),
    maintenance(false);

    private final boolean dataStreamExpected;

}
