package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * Combined JSON serializer and deserializer for LocalTime objects.
 * Handles serialization/deserialization of LocalTime as a string in HH:mm format.
 */
public class LocalTimeJsonConverter
{
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static class Serializer extends JsonSerializer<LocalTime>
    {
        @Override
        public void serialize(LocalTime value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeString(value.format(FORMATTER));
        }
    }

    public static class Deserializer extends JsonDeserializer<LocalTime>
    {
        @Override
        public LocalTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String timeStr = p.getValueAsString();
            return LocalTime.parse(timeStr, FORMATTER);
        }
    }
}
