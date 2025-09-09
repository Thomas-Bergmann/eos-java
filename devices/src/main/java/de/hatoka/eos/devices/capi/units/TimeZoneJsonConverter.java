package de.hatoka.eos.devices.capi.units;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.time.ZoneId;

/**
 * Combined JSON serializer and deserializer for LocalTime objects.
 * Handles serialization/deserialization of LocalTime as a string in HH:mm format.
 */
public class TimeZoneJsonConverter
{
    public static class Serializer extends JsonSerializer<ZoneId>
    {
        @Override
        public void serialize(ZoneId value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeString(value.toString());
        }
    }

    public static class Deserializer extends JsonDeserializer<ZoneId>
    {
        @Override
        public ZoneId deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            String timeZoneStr = p.getValueAsString();
            return ZoneId.of(timeZoneStr);
        }
    }
}