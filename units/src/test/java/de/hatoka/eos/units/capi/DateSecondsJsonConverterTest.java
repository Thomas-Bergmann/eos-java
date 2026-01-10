package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DateSecondsJsonConverterTest
{
    // Jan 10 2026
    private static final Date LAST_REPORT = new Date(1768052921000L);
    public static final String SERIALIZED = "{\"last_report_at\":1768052921}";

    public static class Meta
    {
        @JsonSerialize(using = DateSecondsJsonConverter.Serializer.class)
        @JsonDeserialize(using = DateSecondsJsonConverter.Deserializer.class)
        @JsonProperty("last_report_at")
        private Date lastReport;

        public Meta() {}

        Meta(Date lastReport)
        {
            this.lastReport = lastReport;
        }
    }

    @Test
    void testSerialization() throws JsonProcessingException
    {
        ObjectMapper mapper = new ObjectMapper();
        String serialized = mapper.writeValueAsString(new Meta(LAST_REPORT));
        assertEquals(SERIALIZED, serialized);
        assertEquals(LAST_REPORT, mapper.readValue(SERIALIZED, Meta.class).lastReport);
    }
}
