package de.hatoka.eos.hardware.enphase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.units.capi.DateSecondsJsonConverter;
import de.hatoka.eos.units.capi.Energy;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryProductionResponseRO
{
    @JsonProperty("intervals")
    private IntervalRO[] intervals;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IntervalRO {
        @JsonSerialize(using = DateSecondsJsonConverter.Serializer.class)
        @JsonDeserialize(using = DateSecondsJsonConverter.Deserializer.class)
        @JsonProperty("end_at")
        private Date endAt;

        @JsonProperty("wh_del")
        private int wh;
    }

    @JsonIgnore
    public Map<Date, Energy> getTelemetry()
    {
        Map<Date, Energy> result = new HashMap<>();
        for(IntervalRO inner : intervals)
        {
            result.put(inner.endAt, Energy.ofWh(inner.wh));
        }
        return result;
    }
}

