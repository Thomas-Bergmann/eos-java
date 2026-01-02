package de.hatoka.eos.simulation.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.persistence.capi.weather.WeatherStation;

import java.util.List;

/**
 * The InstallationConfig is the container of all configuration of an installation. It can contain a list of devices (panels, batteries, cars) and how
 * the grid revenue is calculated.
 */
public class InstallationConfig
{
    @JsonProperty("devices")
    @JsonPropertyDescription("List of energy devices in the installation (solar panels, batteries, etc.)")
    private List<DeviceConfig> devices;
    
    @JsonProperty("grid")
    @JsonPropertyDescription("Grid connection configuration including import/export pricing")
    private GridConfig grid;

    @JsonProperty("weatherStation")
    @JsonPropertyDescription("Weather station to use for weather forecasts (APOLDA, LEIPZIG_STADTWERKE)")
    private WeatherStation weatherStation = WeatherStation.APOLDA; // default value

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

    public WeatherStation getWeatherStation()
    {
        return weatherStation;
    }

    public void setWeatherStation(WeatherStation weatherStation)
    {
        this.weatherStation = weatherStation;
    }
}