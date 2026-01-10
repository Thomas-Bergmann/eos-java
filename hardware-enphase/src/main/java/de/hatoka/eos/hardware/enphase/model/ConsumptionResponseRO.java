package de.hatoka.eos.hardware.enphase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.units.capi.DateSecondsJsonConverter;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConsumptionResponseRO
{
    @JsonProperty("consumption")
    private int[] consumption;
    @JsonProperty("meta")
    private Meta meta;

    public int[] getConsumption()
    {
        return consumption;
    }

    public Meta getMeta()
    {
        return meta;
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Meta {
        @JsonProperty("consumption")
        private String status;
        @JsonSerialize(using = DateSecondsJsonConverter.Serializer.class)
        @JsonDeserialize(using = DateSecondsJsonConverter.Deserializer.class)
        @JsonProperty("last_report_at")
        private Date lastReport;
        @JsonSerialize(using = DateSecondsJsonConverter.Serializer.class)
        @JsonDeserialize(using = DateSecondsJsonConverter.Deserializer.class)
        @JsonProperty("last_energy_at")
        private Date last_energy;
        @JsonSerialize(using = DateSecondsJsonConverter.Serializer.class)
        @JsonDeserialize(using = DateSecondsJsonConverter.Deserializer.class)
        @JsonProperty("operational_at")
        private Date operational;
    }
}

