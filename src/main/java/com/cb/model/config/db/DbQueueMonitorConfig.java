package com.cb.model.config.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DbQueueMonitorConfig {

    private long id;
    private String queue_name;
    private int message_limit;

}
