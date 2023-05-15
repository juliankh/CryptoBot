package com.cb.model.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Instant;

public class InstantAdapter extends TypeAdapter<Instant> {

    @Override
    public Instant read(JsonReader reader) throws IOException {
        return Instant.ofEpochMilli(reader.nextLong());
    }

    @Override
    public void write(JsonWriter writer, Instant instant) throws IOException {
        writer.value(instant.toEpochMilli());
    }

}