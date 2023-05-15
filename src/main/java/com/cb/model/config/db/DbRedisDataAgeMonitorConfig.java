package com.cb.model.config.db;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DbRedisDataAgeMonitorConfig {

    private long id;
    private String redis_key;
    private int mins_age_limit;

}
