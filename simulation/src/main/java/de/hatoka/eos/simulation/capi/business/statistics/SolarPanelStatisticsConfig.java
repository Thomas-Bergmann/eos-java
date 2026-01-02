package de.hatoka.eos.simulation.capi.business.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class SolarPanelStatisticsConfig
{
    @JsonProperty("hourly_efficiency")
    @JsonPropertyDescription("List of hourly efficiency values for solar panels")
    private List<HourlyEfficiency> hourlyEfficiency;

    public List<HourlyEfficiency> getHourlyEfficiency()
    {
        return hourlyEfficiency;
    }

    public void setHourlyEfficiency(List<HourlyEfficiency> hourlyEfficiency)
    {
        this.hourlyEfficiency = hourlyEfficiency;
    }
}