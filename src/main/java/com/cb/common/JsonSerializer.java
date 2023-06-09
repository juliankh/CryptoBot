package com.cb.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;

import javax.inject.Singleton;

// This class exists in order to be able to call ObjectMatter without having to catch exceptions, which is useful when doing these transformations using streams
@Singleton
public class JsonSerializer {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper().registerModule(new JavaTimeModule());

    @SneakyThrows
    public String serializeToJson(Object o) {
        return OBJECT_MAPPER.writer().writeValueAsString(o);
    }

    @SneakyThrows
    public <T> T deserializeFromJson(String json, Class<T> clazz) {
        return OBJECT_MAPPER.readValue(json, clazz);
    }

}
