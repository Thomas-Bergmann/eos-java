package de.hatoka.eos.devices.capi.units;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Combined JSON serializer and deserializer for LocalDateTime objects.
 * Handles serialization/deserialization of LocalTime as a string in yyyy/MM/dd-HH:mm format.
 */
public class LocalDateTimeJsonConverter
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm");

    public static class Serializer extends JsonSerializer<LocalDateTime>
    {
        @Override
        public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeString(value.format(FORMATTER));
        }
    }

    public static class Deserializer extends JsonDeserializer<LocalDateTime>
    {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String timeStr = p.getValueAsString();
            return LocalDateTime.parse(timeStr, FORMATTER);
        }
    }
}