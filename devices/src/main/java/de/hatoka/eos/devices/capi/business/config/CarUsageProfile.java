package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.LocalTimeJsonConverter;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

/**
 * The CarUsageProfile defines, how often, how long the car is used, and what is the energy consumption if used
 * @param days week days the car is used
 * @param startUsage start time usage (car can't be loaded afterward)
 * @param endUsage end time usage (car is available for loading and energy consumption, is "reduced" from car battery)
 * @param energyConsumption energy consumption per usage
 */
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