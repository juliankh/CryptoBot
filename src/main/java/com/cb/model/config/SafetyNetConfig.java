package com.cb.model.config;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@ToString
@EqualsAndHashCode
public class SafetyNetConfig {

    private long id;
    private String processToken;
    private String processSubToken;
    private boolean active;

}
