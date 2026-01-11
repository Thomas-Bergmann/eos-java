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
public class TelemetryBatteryResponseRO
{
    @JsonProperty("intervals")
    private IntervalRO[] intervals;

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class IntervalRO {
        @JsonSerialize(using = DateSecondsJsonConverter.Serializer.class)
        @JsonDeserialize(using = DateSecondsJsonConverter.Deserializer.class)
        @JsonProperty("end_at")
        private Date endAt;

        @JsonProperty("charge")
        private EnergyRO charge;
        @JsonProperty("discharge")
        private EnergyRO discharge;
        @JsonProperty("soc")
        private SocRO soc;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SocRO {
        @JsonProperty("percent")
        private int percent;
    }
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class EnergyRO {
        @JsonProperty("enwh")
        private int wh;
    }

    public record BatteryInterval(Percentage percentage, Energy charge, Energy discharge) {};

    @JsonIgnore
    public Map<Date, BatteryInterval> getTelemetry()
    {
        Map<Date, BatteryInterval> result = new HashMap<>();
        for(IntervalRO intervalRO : intervals)
        {
            result.put(intervalRO.endAt, new BatteryInterval(Percentage.valueOfPercent(intervalRO.soc.percent), Energy.ofWh(intervalRO.charge.wh), Energy.ofWh(intervalRO.discharge.wh)));
        }
        return result;
    }
}

