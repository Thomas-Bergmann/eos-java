package de.hatoka.eos.forecast.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Data Transfer Object for OpenMeteo API response.
 * Represents the weather forecast response from the OpenMeteo API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class OpenMeteoResponse
{
    @JsonProperty("latitude")
    private double latitude;

    @JsonProperty("longitude")
    private double longitude;

    @JsonProperty("timezone")
    private String timezone;

    @JsonProperty("hourly")
    private HourlyData hourly;

    // Constructors, getters, and setters
    public OpenMeteoResponse()
    {
    }

    public double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(double latitude)
    {
        this.latitude = latitude;
    }

    public double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(double longitude)
    {
        this.longitude = longitude;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }

    public HourlyData getHourly()
    {
        return hourly;
    }

    public void setHourly(HourlyData hourly)
    {
        this.hourly = hourly;
    }
}