package de.hatoka.eos.devices.capi.units;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Combined JSON serializer and deserializer for Power objects.
 * Handles serialization/deserialization of Power as an object with "amount" and "unit" fields.
 */
public class PowerJsonConverter
{
    public static class Serializer extends JsonSerializer<Power>
    {
        @Override
        public void serialize(Power value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeStartObject();
            gen.writeNumberField("amount", value.amount());
            gen.writeStringField("unit", value.unit().name());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Power>
    {
        @Override
        public Power deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            JsonNode node = p.getCodec().readTree(p);
            
            double amount = node.get("amount").asDouble();
            String unitStr = node.get("unit").asText();
            PowerUnits unit = PowerUnits.valueOf(unitStr);
            
            return new Power(amount, unit);
        }
    }
}