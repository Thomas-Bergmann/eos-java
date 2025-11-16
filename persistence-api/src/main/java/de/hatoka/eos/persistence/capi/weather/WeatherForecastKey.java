package de.hatoka.eos.persistence.capi.weather;

import java.time.ZonedDateTime;

public record WeatherForecastKey(String station, ZonedDateTime time, WeatherDataSource source)
{
    public static WeatherForecastKey valueOf(WeatherStation station, ZonedDateTime time, WeatherDataSource source)
    {
        return new WeatherForecastKey(station.name(), time, source);
    }
}
