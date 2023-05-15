package com.cb.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class RedisDataAgeMonitorConfig {

    private long id;
    private String redisKey;
    private int minsAgeLimit;

}
