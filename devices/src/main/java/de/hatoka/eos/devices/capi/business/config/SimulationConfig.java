package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class SimulationConfig
{
    @JsonProperty("id")
    @JsonPropertyDescription("simulation id (for metric export")
    private String id;

    @JsonProperty("simulation")
    @JsonPropertyDescription("time settings of simulation")
    private SimulationTimeSettings timeSettings;

    public SimulationTimeSettings getTimeSettings()
    {
        return timeSettings;
    }

    public String getId()
    {
        return id;
    }
}