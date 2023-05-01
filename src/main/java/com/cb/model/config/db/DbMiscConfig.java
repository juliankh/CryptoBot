package com.cb.model.config.db;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DbMiscConfig {

    private long id;
    private String name;
    private double value;

}
