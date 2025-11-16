package de.hatoka.eos.simulation.internal.business.forecast;

import de.hatoka.eos.simulation.capi.business.forecast.WeatherForecast;
import de.hatoka.eos.persistence.capi.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.WeatherDataSource;
import de.hatoka.eos.persistence.influx.InfluxWeatherForecastDao;
import de.hatoka.eos.units.capi.Percentage;

import java.time.ZonedDateTime;

/**
 * Weather forecast implementation that reads from InfluxDB via ForecastDAO.
 */
public class InfluxWeatherForecast implements WeatherForecast
{
    private final String station;
    private final InfluxWeatherForecastDao forecastDAO;

    public InfluxWeatherForecast(String station, InfluxWeatherForecastDao forecastDAO)
    {
        this.station = station;
        this.forecastDAO = forecastDAO;
    }
    @Override
    public Percentage getSunProbability(ZonedDateTime time)
    {
        return forecastDAO.get(new WeatherForecastKey(station, time, WeatherDataSource.METEOMEDIA)).getSunProbability();
    }
}
