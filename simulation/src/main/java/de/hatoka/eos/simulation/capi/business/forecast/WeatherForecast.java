package de.hatoka.eos.simulation.capi.business.forecast;

import de.hatoka.eos.units.capi.Percentage;

import java.time.ZonedDateTime;

/**
 * WeatherForecast provides sun probability of specific time.
 */
public interface WeatherForecast
{
    /**
     * Provides the intensity of how much sun energy can be collected by a panel in optimum at that time.
     * So it combines the sun minutes per hour multiplied with cloudiness (mega cloudy = 0 to no clouds = 1)
     * @param time start time
     * @return probability how much sun energy can be collected by the panel (in optimal position with 100% efficiency)
     */
    Percentage getSunProbability(ZonedDateTime time);
}