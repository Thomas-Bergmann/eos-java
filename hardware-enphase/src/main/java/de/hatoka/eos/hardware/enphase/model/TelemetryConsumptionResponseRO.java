package de.hatoka.eos.hardware.enphase.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.units.capi.DateSecondsJsonConverter;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Percentage;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelemetryConsumptionResponseRO
{
    @JsonProperty("intervals")
    private IntervalRO[] intervals;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IntervalRO {
        @JsonSerialize(using = DateSecondsJsonConverter.Serializer.class)
        @JsonDeserialize(using = DateSecondsJsonConverter.Deserializer.class)
        @JsonProperty("end_at")
        private Date endAt;

        @JsonProperty("enwh")
        private int wh;
    }

    @JsonIgnore
    public Map<Date, Energy> getConsumption()
    {
        Map<Date, Energy> result = new HashMap<>();
        for(IntervalRO intervalRO : intervals)
        {
            result.put(intervalRO.endAt, Energy.ofWh(intervalRO.wh));
        }
        return result;
    }
}

