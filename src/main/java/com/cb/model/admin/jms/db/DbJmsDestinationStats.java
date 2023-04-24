package com.cb.model.admin.jms.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DbJmsDestinationStats {

    private Long id; // db field
    private String name;
    private Timestamp measured;
    private int messages;
    private int consumers;
    private Timestamp created; // db field

}
