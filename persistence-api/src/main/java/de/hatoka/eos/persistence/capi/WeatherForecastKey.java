package de.hatoka.eos.persistence.capi;

import java.time.ZonedDateTime;

public record WeatherForecastKey(String station, ZonedDateTime time)
{
    public static WeatherForecastKey valueOf(MeteoMediaStation station, ZonedDateTime time)
    {
        return new WeatherForecastKey(station.name(), time);
    }
}
