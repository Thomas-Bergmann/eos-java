package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Date;

/**
 * Combined JSON serializer and deserializer for LocalDateTime objects.
 * Handles serialization/deserialization of LocalDateTime as a string in yyyy/MM/dd-HH:mm format.
 */
public class EnergyWattHoursJsonConverter
{
    public static class Serializer extends JsonSerializer<Energy>
    {
        @Override
        public void serialize(Energy value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeNumber(value.convert(EnergyUnits.WH).amount());
        }
    }

    public static class Deserializer extends JsonDeserializer<Energy>
    {
        @Override
        public Energy deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            return Energy.ofWh(p.getValueAsLong());
        }
    }
}
