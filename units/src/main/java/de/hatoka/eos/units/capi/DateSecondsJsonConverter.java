package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * Combined JSON serializer and deserializer for LocalDateTime objects.
 * Handles serialization/deserialization of LocalDateTime as a string in yyyy/MM/dd-HH:mm format.
 */
public class DateSecondsJsonConverter
{
    public static class Serializer extends JsonSerializer<Date>
    {
        @Override
        public void serialize(Date value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeNumber(value.getTime() / 1000);
        }
    }

    public static class Deserializer extends JsonDeserializer<Date>
    {
        @Override
        public Date deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            return new Date(p.getValueAsLong() * 1000);
        }
    }
}
