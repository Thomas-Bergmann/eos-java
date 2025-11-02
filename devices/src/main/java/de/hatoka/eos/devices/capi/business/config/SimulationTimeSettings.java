package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.units.capi.LocalDateTimeJsonConverter;
import de.hatoka.eos.units.capi.TimeZoneJsonConverter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class SimulationTimeSettings
{
    @JsonProperty("timezone")
    @JsonPropertyDescription("Timezone identifier (e.g., 'Europe/Berlin') for the installation location")
    @JsonSerialize(using = TimeZoneJsonConverter.Serializer.class)
    @JsonDeserialize(using = TimeZoneJsonConverter.Deserializer.class)
    private ZoneId timezone = ZoneId.of("Europe/Berlin");

    @JsonProperty("start")
    @JsonPropertyDescription("Start time of simulation (format: yyyy/MM/dd-HH:mm)")
    @JsonSerialize(using = LocalDateTimeJsonConverter.Serializer.class)
    @JsonDeserialize(using = LocalDateTimeJsonConverter.Deserializer.class)
    private LocalDateTime startTime;

    @JsonProperty("end")
    @JsonPropertyDescription("End time of simulation (format: yyyy/MM/dd-HH:mm)")
    @JsonSerialize(using = LocalDateTimeJsonConverter.Serializer.class)
    @JsonDeserialize(using = LocalDateTimeJsonConverter.Deserializer.class)
    private LocalDateTime endTime;

    @JsonProperty("stepDuration")
    @JsonPropertyDescription("Duration of each simulation step (format: java duration; default 15min)")
    private Duration stepDuration = Duration.ofMinutes(15);

    @JsonIgnore
    public ZonedDateTime getZonedStartTime()
    {
        return startTime.atZone(timezone);
    }
    @JsonIgnore
    public ZonedDateTime getZonedEndTime()
    {
        return endTime.atZone(timezone);
    }

    public ZoneId getTimezone()
    {
        return timezone;
    }

    public Duration getStepDuration()
    {
        return stepDuration;
    }
}