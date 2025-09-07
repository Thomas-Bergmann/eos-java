package de.hatoka.eos.devices.capi.units;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * Combined JSON serializer and deserializer for Power objects.
 * Handles serialization/deserialization of Power as an object with "amount" and "unit" fields.
 */
public class MoneyJsonConverter
{
    public static class Serializer extends JsonSerializer<Money>
    {
        @Override
        public void serialize(Money value, JsonGenerator gen, SerializerProvider serializers) throws IOException
        {
            gen.writeStartObject();
            gen.writeNumberField("amount", value.amount());
            gen.writeStringField("currency", value.currencyMnemonic());
            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<Money>
    {
        @Override
        public Money deserialize(JsonParser p, DeserializationContext ctxt) throws IOException
        {
            JsonNode node = p.getCodec().readTree(p);
            
            double amount = node.get("amount").asDouble();
            String currencyMnemonic = node.get("currency").asText();
            return new Money(new BigDecimal(amount, MathContext.DECIMAL32), currencyMnemonic);
        }
    }
}