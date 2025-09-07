package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public class InstallationConfig
{
    @JsonProperty("installation")
    @JsonPropertyDescription("General installation information including location and timezone")
    private InstallationInfo installation;

    @JsonProperty("devices")
    @JsonPropertyDescription("List of energy devices in the installation (solar panels, batteries, etc.)")
    private List<DeviceConfig> devices;
    
    @JsonProperty("grid")
    @JsonPropertyDescription("Grid connection configuration including import/export pricing")
    private GridConfig grid;
    

    public InstallationInfo getInstallation()
    {
        return installation;
    }

    public void setInstallation(InstallationInfo installation)
    {
        this.installation = installation;
    }

    public List<DeviceConfig> getDevices()
    {
        return devices;
    }

    public void setDevices(List<DeviceConfig> devices)
    {
        this.devices = devices;
    }

    public GridConfig getGrid()
    {
        return grid;
    }

    public void setGrid(GridConfig grid)
    {
        this.grid = grid;
    }

}