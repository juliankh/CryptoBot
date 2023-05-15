package com.cb.model.config.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DbRedisDataCleanerConfig {

    private long id;
    private String redis_key;
    private int mins_back;

}
