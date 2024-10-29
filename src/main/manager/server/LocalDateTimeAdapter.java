package main.manager.server;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME; // статический форматтер

    // Сериализация LocalDateTime в JSON
    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        if (localDateTime != null) {
            jsonWriter.value(localDateTime.format(FORMATTER));
        } else {
            jsonWriter.nullValue();
        }
    }

    // Десериализация JSON в LocalDateTime
    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String text = jsonReader.nextString();
        if ("null".equals(text)) {
            return null;
        }
        try {
            return LocalDateTime.parse(text, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new IOException("Ошибка парсинга LocalDateTime: неверный формат даты-времени '" + text + "'", e);
        }
    }
}