package com.cb.model.config.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Deprecated
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DbDataCleanerConfig {

    private long id;
    private String table_name;
    private String column_name;
    private int hours_back;

}
