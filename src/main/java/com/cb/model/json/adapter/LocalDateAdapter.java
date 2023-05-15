package com.cb.model.json.adapter;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalDateAdapter extends TypeAdapter<LocalDate> {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    @Override
    public LocalDate read(JsonReader reader) throws IOException {
        return LocalDate.parse(reader.nextString(), FORMATTER);
    }

    @Override
    public void write(JsonWriter writer, LocalDate date) throws IOException {
        writer.value(date.format(FORMATTER));
    }

}