package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Combined JSON serializer and deserializer for Energy objects.
 * Handles serialization/deserialization of Energy as an object with "amount" and "unit" fields.
 */
public class EnergyJsonConverter
{
    public static class Serializer extends JsonSerializer<Energy>
    {
        @Override
        public void serialize(Energy value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeStartObject();
            gen.writeNumberField("amount", value.amount());
            gen.writeStringField("unit", value.unit().name());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Energy>
    {
        @Override
        public Energy deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            JsonNode node = p.getCodec().readTree(p);

            double amount = node.get("amount").asDouble();
            String unitStr = node.get("unit").asText();
            EnergyUnits unit = EnergyUnits.valueOf(unitStr);

            return new Energy(amount, unit);
        }
    }
}
