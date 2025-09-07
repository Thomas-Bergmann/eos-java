package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.devices.capi.units.Energy;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public record CarUsageProfile(
    @JsonProperty("days")
    @JsonPropertyDescription("Days of the week when the car is used (e.g., [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY])")
    List<DayOfWeek> days,

    @JsonProperty("startUsage")
    @JsonPropertyDescription("Time when the car leaves home (format: HH:MM)")
    @JsonSerialize(using = LocalTimeJsonConverter.Serializer.class)
    @JsonDeserialize(using = LocalTimeJsonConverter.Deserializer.class)
    LocalTime startUsage,

    @JsonProperty("endUsage")
    @JsonPropertyDescription("Time when the car returns home (format: HH:MM)")
    @JsonSerialize(using = LocalTimeJsonConverter.Serializer.class)
    @JsonDeserialize(using = LocalTimeJsonConverter.Deserializer.class)
    LocalTime endUsage,

    @JsonProperty("energyConsumption")
    @JsonPropertyDescription("Total energy consumed during the usage period")
    Energy energyConsumption
)
{
}