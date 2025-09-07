package de.hatoka.eos.devices.capi.business.metrics;

import de.hatoka.eos.devices.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.devices.capi.business.forecast.WeatherForecast;

import java.time.ZonedDateTime;

/**
 * Interface for exporting simulation metrics to time-series databases like InfluxDB.
 */
public interface ForecastMetricsExporter
{
    /**
     * Exports forecasts of given time
     *
     * @param time time of forecast event
     * @param energyPriceProvider energy price provider provides price for given time
     */
    void export(ZonedDateTime time, EnergyPriceForecast energyPriceProvider);

    /**
     * Exports forecasts of given time
     *
     * @param time time of forecast event
     * @param weatherService weather service provides sun probability for given time
     */
    void export(ZonedDateTime time, WeatherForecast weatherService);
}