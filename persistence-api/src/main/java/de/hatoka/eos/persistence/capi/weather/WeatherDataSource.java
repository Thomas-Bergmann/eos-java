package de.hatoka.eos.persistence.capi.weather;

/**
 * Enumeration of weather data sources.
 */
public enum WeatherDataSource
{
    METEOMEDIA("meteomedia"),
    OPENMETEO("openmeteo"),
    TEST("test");

    private final String identifier;

    WeatherDataSource(String identifier)
    {
        this.identifier = identifier;
    }

    public String getIdentifier()
    {
        return identifier;
    }

    @Override
    public String toString()
    {
        return identifier;
    }
}