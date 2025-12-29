# Configuration Module

## Overview

The configuration module provides a centralized way to configure EOS (Energy Optimization System) installations including:
- Weather station selection for weather forecasts
- Device configurations (solar panels, batteries, electric cars, etc.)
- Grid pricing models
- Simulation parameters

## Business Configuration (YAML)

Business configuration is stored in YAML files and can be loaded using the `ConfigurationLoader` class. This is separate from infrastructure configuration (application.properties).

### Installation Configuration

Installation configuration defines the physical setup of an energy system.

#### Example: `test-installation.yaml`

```yaml
devices:
  - type: SOLAR_PANEL
    production:
      amount: 0.45
      unit: "K_W"
    statisticsResource: "curved"
    panelEfficiency: .92
    count: 13

  - type: BATTERY
    capacity:
      amount: 3.0
      unit: "K_WH"
    chargeRate:
      amount: 2.2
      unit: "K_W"
    dischargeRate:
      amount: 2.1
      unit: "K_W"
    count: 3
    chargingEfficiency: 0.85
    dischargingEfficiency: 0.85

  - type: GRID
    count: 1

# Weather station configuration for forecasts
# Available stations: APOLDA, LEIPZIG_STADTWERKE
weatherStation: APOLDA

grid:
  type: "FLAT"
  flatPriceProvider:
    importPrice:
      amount: 0.39
      currency: EUR
    exportPrice:
      amount: 0.08
      currency: EUR
```

### Weather Station Configuration

The `weatherStation` field in the installation configuration specifies which weather station to use for weather forecasts.

Available weather stations:
- **APOLDA**: Located at 51.0262°N, 11.5164°E (Station #095550)
- **LEIPZIG_STADTWERKE**: Located at 51.3397°N, 12.3731°E (Station #104700)

Example:
```yaml
weatherStation: APOLDA
```

If not specified, defaults to `APOLDA`.

### Loading Configuration

```java
@Inject
private ConfigurationLoader configurationLoader;

@Inject
private ForecastsFactory forecastsFactory;

// Load installation configuration
InstallationConfig config = configurationLoader.loadInstallation("my-installation.yaml");

// Create forecasts from configuration
Forecasts forecasts = forecastsFactory.createForecasts(config);

// Use in simulation
SimulationRequest request = new SimulationRequest(
    "simulation-id",
    startDate,
    endDate,
    stepDuration,
    devices,
    initialState,
    forecasts  // Uses the weather station configured in YAML
);
```

## Key Classes

### `InstallationConfig`
Contains the complete configuration of an energy installation:
- `devices`: List of device configurations
- `grid`: Grid pricing configuration  
- `weatherStation`: Weather station enum for forecasts

### `ForecastsFactory`
Factory class that creates `Forecasts` objects from `InstallationConfig`:
- Automatically creates `DaoWeatherForecast` with the configured weather station
- Falls back to default flat forecasts if configuration is missing

### `DaoWeatherForecast`
Weather forecast implementation that reads from InfluxDB:
- Uses the configured `WeatherStation` to fetch forecast data
- Accesses data via `WeatherForcastDAO`

## Adding New Weather Stations

To add a new weather station:

1. Add the station to `WeatherStation` enum in `persistence-api`:
```java
public enum WeatherStation {
    APOLDA("095550", 51.0262, 11.5164),
    LEIPZIG_STADTWERKE("104700", 51.3397, 12.3731),
    NEW_STATION("123456", 52.5200, 13.4050);  // Add here
    // ...
}
```

2. Update your YAML configuration:
```yaml
weatherStation: NEW_STATION
```

## Migration Guide

If you're using the old hardcoded weather station approach:

**Before:**
```java
// Hardcoded station
Forecasts forecasts = Forecasts.STANDARD;
```

**After:**
```java
// Load from configuration
InstallationConfig config = configurationLoader.loadInstallation("my-installation.yaml");
Forecasts forecasts = forecastsFactory.createForecasts(config);
```

## Testing

Test configurations can be found in:
- `simulation/src/test/resources/test-installation-*.yaml`
- `optimization/src/test/resources/test-installation-for-optimization.yaml`

Example test usage:
```java
@Inject
private ConfigurationLoader configurationLoader;

@Inject
private ForecastsFactory forecastsFactory;

@Test
public void testWithConfiguration() {
    InstallationConfig config = configurationLoader.loadInstallation("test-installation-with-car.yaml");
    Forecasts forecasts = forecastsFactory.createForecasts(config);
    
    // forecasts.weather() will use the weather station from YAML
}
```

