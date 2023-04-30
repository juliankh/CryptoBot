package com.cb.model.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@Accessors(chain = true)
public class DataCleanerConfig {

    private long id;
    private String tableName;
    private String columnName;
    private int hoursBack;

}
