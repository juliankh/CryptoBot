package com.cb.model.config.db;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class DbProcessConfig {

    private long id;
    private String process_token;
    private String process_subtoken;
    private boolean active;

}
