package de.hatoka.eos.persistence.capi;

public enum MeteoMediaStation
{
    APOLDA("095550"),
    LEIPZIG_STADTWERKE("104700");

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
