package de.hatoka.eos.simulation.internal.business.forecast;

import de.hatoka.eos.simulation.capi.business.forecast.WeatherForecast;
import de.hatoka.eos.units.capi.Percentage;

import java.time.ZonedDateTime;

public class FlatWeatherService implements WeatherForecast
{
    public static final WeatherForecast FULL_FROM_7_to_18 = new FlatWeatherService(7, 18);

    private final int startHour;
    private final int endHour;

    public FlatWeatherService(int startHour, int endHour)
    {
        this.startHour = startHour;
        this.endHour = endHour;
    }

    @Override
    public Percentage getSunProbability(ZonedDateTime zonedTime)
    {
        // Convert Date to ZonedDateTime using the configured timezone
        int hour = zonedTime.getHour();

        // Outside sun hours
        if (hour <= startHour || hour >= endHour)
        {
            return Percentage.ZERO;
        }
        if (hour - startHour < 2 || endHour - hour < 2)
        {
            return new Percentage(0.9);
        }
        return Percentage.ONE_HUNDRED;
    }
}