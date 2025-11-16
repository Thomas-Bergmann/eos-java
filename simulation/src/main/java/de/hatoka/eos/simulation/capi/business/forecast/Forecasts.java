package de.hatoka.eos.simulation.capi.business.forecast;

import de.hatoka.eos.simulation.internal.business.forecast.FlatPriceService;
import de.hatoka.eos.simulation.internal.business.forecast.FlatWeatherService;

/**
 * Collection of all forecasts
 * @param weather weather forecast
 * @param priceForecast energy price forecast
 */
public record Forecasts(WeatherForecast weather, EnergyPriceForecast priceForecast)
{
    public static final Forecasts STANDARD = new Forecasts(FlatWeatherService.FULL_FROM_7_to_18, FlatPriceService.GERMAN_RESIDENTIAL);
}
