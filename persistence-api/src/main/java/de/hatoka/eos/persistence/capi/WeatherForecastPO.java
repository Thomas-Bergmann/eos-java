package de.hatoka.eos.persistence.capi;

import de.hatoka.eos.units.capi.Percentage;

public class WeatherForecastPO
{
    public static final String COLUMN_SUN_PROBABILITY = "sun_probability";
    private Percentage sunProbability;

    public Percentage getSunProbability()
    {
        return sunProbability;
    }

    public void setSunProbability(Percentage sunProbability)
    {
        this.sunProbability = sunProbability;
    }
}
