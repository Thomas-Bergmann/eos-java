package de.hatoka.eos.persistence.capi.weather;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @param station weather station
 * @param time epoch milli seconds
 * @param source weather forecast source
 */
public record WeatherForecastKey(WeatherStation station, long time, WeatherDataSource source)
{
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static WeatherForecastKey valueOf(WeatherStation station, ZonedDateTime time, WeatherDataSource source)
    {
        return new WeatherForecastKey(station, time.toInstant().toEpochMilli(), source);
    }

    public Instant getInstant()
    {
        return Instant.ofEpochMilli(time);
    }

    public ZonedDateTime getZonedDateTime()
    {
        return getInstant().atZone(UTC);
    }
}
