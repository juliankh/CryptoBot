package com.cb.model.json.adapter;

import com.fasterxml.jackson.databind.util.StdConverter;

import java.time.Instant;

public class InstantToLongConverter extends StdConverter<Instant, Long> {

    public Long convert(final Instant value) {
        return value.toEpochMilli();
    }

}
