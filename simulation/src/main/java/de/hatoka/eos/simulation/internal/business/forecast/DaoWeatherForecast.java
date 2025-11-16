package de.hatoka.eos.simulation.internal.business.forecast;

import de.hatoka.eos.persistence.capi.weather.WeatherDataSource;
import de.hatoka.eos.persistence.capi.weather.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastKey;
import de.hatoka.eos.simulation.capi.business.forecast.WeatherForecast;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;

import java.time.ZonedDateTime;

/**
 * Weather forecast implementation that reads from InfluxDB via ForecastDAO.
 */
public class DaoWeatherForecast implements WeatherForecast
{
    private final String station;
    @Inject
    private WeatherForcastDAO forecastDAO;

    public DaoWeatherForecast(String station)
    {
        this.station = station;
    }
    @Override
    public Percentage getSunProbability(ZonedDateTime time)
    {
        return forecastDAO.get(new WeatherForecastKey(station, time, WeatherDataSource.METEOMEDIA)).getSunProbability();
    }
}
