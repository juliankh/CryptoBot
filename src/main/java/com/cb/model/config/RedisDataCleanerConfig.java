package com.cb.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class RedisDataCleanerConfig {

    private long id;
    private String redisKey;
    private int minsBack;

}
