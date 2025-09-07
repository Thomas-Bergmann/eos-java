package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

public class InstallationInfo
{
    @JsonProperty("location")
    @JsonPropertyDescription("Geographic location of the energy installation")
    private String location;
    
    @JsonProperty("timezone")
    @JsonPropertyDescription("Timezone identifier (e.g., 'Europe/Berlin') for the installation location")
    private String timezone;

    public String getLocation()
    {
        return location;
    }

    public void setLocation(String location)
    {
        this.location = location;
    }

    public String getTimezone()
    {
        return timezone;
    }

    public void setTimezone(String timezone)
    {
        this.timezone = timezone;
    }
}