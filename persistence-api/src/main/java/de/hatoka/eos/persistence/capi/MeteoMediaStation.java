package de.hatoka.eos.persistence.capi;

/**
 * Weather stations with their MeteoMedia station numbers and geographic coordinates for OpenMeteo API.
 */
public enum MeteoMediaStation
{
    APOLDA("095550", 51.0262, 11.5164),
    LEIPZIG_STADTWERKE("104700", 51.3397, 12.3731);

    private final String stationNumber;
    private final double latitude;
    private final double longitude;

    MeteoMediaStation(String stationNumber, double latitude, double longitude)
    {
        this.stationNumber = stationNumber;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getStationNumber()
    {
        return stationNumber;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }
}
