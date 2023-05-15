package com.cb.model.config.db.archived;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DbDataAgeMonitorConfig {

    private long id;
    private String table_name;
    private String column_name;
    private int mins_age_limit;

}
