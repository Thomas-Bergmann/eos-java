package de.hatoka.eos.persistence.capi;

import de.hatoka.eos.units.capi.Percentage;

public class WeatherForecastPO
{
    public static final String COLUMN_SUN_PROBABILITY = "sun_probability";
    private Percentage sunProbability;

    public static final String COLUMN_STATION = "station";
    private MeteoMediaStation station;

    public Percentage getSunProbability()
    {
        return sunProbability;
    }

    public void setSunProbability(Percentage sunProbability)
    {
        this.sunProbability = sunProbability;
    }

    public MeteoMediaStation getStation()
    {
        return station;
    }

    public void setStation(MeteoMediaStation station)
    {
        this.station = station;
    }
}
