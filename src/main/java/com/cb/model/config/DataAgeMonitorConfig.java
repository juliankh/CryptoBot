package com.cb.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
public class DataAgeMonitorConfig {

    private long id;
    private String tableName;
    private String columnName;
    private int minsAgeLimit;

}
