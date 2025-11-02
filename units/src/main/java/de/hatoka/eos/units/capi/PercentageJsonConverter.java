package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Combined JSON serializer and deserializer for Percentage objects.
 * Handles serialization/deserialization of Percentage as a numeric value representing the fraction (0.0 to 1.0).
 */
public class PercentageJsonConverter
{
    public static class Serializer extends JsonSerializer<Percentage>
    {
        @Override
        public void serialize(Percentage value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeNumber(value.value());
        }
    }

    public static class Deserializer extends JsonDeserializer<Percentage>
    {
        @Override
        public Percentage deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            double value = p.getDoubleValue();
            return new Percentage(value);
        }
    }
}
