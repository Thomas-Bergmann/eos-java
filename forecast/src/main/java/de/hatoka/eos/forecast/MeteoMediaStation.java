package de.hatoka.eos.forecast;

public enum MeteoMediaStation
{
    APOLDA("095550");

    private final String stationNumber;

    MeteoMediaStation(String stationNumber)
    {
        this.stationNumber = stationNumber;
    }

    public String getStationNumber()
    {
        return stationNumber;
    }
}
