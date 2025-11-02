package de.hatoka.eos.devices.internal.business.forecast;

import de.hatoka.eos.devices.capi.business.forecast.WeatherForecast;
import de.hatoka.eos.persistence.influx.InfluxWeatherForecastDao;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.ZonedDateTime;

/**
 * Weather forecast implementation that reads from InfluxDB via ForecastDAO.
 */
@Singleton
public class InfluxWeatherForecast implements WeatherForecast
{
    @Inject
    InfluxWeatherForecastDao forecastDAO;

    @Override
    public Percentage getSunProbability(ZonedDateTime time)
    {
        return forecastDAO.get(time).getSunProbability();
    }
}
