package de.hatoka.eos.forecast.openmeteo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * Hourly weather data from OpenMeteo API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class HourlyData
{
    @JsonProperty("time")
    private List<String> time;

    @JsonProperty("sunshine_duration")
    private List<Double> sunshineDuration;

    public HourlyData()
    {
    }

    public List<String> getTime()
    {
        return time;
    }

    public void setTime(List<String> time)
    {
        this.time = time;
    }

    public List<Double> getSunshineDuration()
    {
        return sunshineDuration;
    }

    public void setSunshineDuration(List<Double> sunshineDuration)
    {
        this.sunshineDuration = sunshineDuration;
    }
}