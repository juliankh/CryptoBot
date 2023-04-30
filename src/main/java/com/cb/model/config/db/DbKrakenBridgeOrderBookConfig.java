package com.cb.model.config.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DbKrakenBridgeOrderBookConfig {

    private long id;
    private String currency_base;
    private String currency_counter;
    private int batch_size;
    private int secs_timeout;

}
