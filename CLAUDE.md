# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Common Commands

### Build and Test
- `./gradlew test` - Run all JUnit tests
- `./gradlew build` - Full build including tests, JAR creation, and code coverage
- `./gradlew jacocoTestReport` - Generate code coverage reports (outputs to `build/reports/jacoco/`)

### Running Single Tests
- `./gradlew test --tests "de.hatoka.eos.*Test"`
- `./gradlew test --tests "*DeviceBuilderTest"`

## Architecture Overview

This is a Java-based Energy System Simulation (EOS) project that models energy devices and their interactions over time.

### Module Structure
- **devices**: Main module containing device implementations and simulation engine
- **versions/versions-test**: Dependency management modules

### Core Components

**Device System** (`devices/src/main/java/de/hatoka/eos/devices/`)
- `Device`: Interface for energy devices with `simulate(SimulationStep, EnergySystem, DeviceState)` method
- `DeviceType`: Enum defining device types with factory methods
- `DeviceBuilder`: Fluent builder for creating device configurations
- Device implementations: `Battery`, `SolarPanel`, `Grid`, `NoisyUsage`, `ElectricCar`

**Simulation Engine** (`devices/src/main/java/de/hatoka/eos/devices/internal/business/simulation/`)
- `Simulation`: Core simulation orchestrator that executes time-stepped energy calculations
- `SimulationStep`: Represents a single time slot in the simulation with charging and grid configuration
- `EnergySystem`: Tracks all energy flows (production, consumption, charging, discharging, grid import/export, costs)
- `SimulationStepResult`: Contains the energy system state and device state after simulation step
- `DeviceState`: Tracks device state across simulation steps

**Units System** (`devices/src/main/java/de/hatoka/eos/devices/capi/units/`)
- `Energy`: Energy values with kWh units and arithmetic operations
- `Power`: Power values with kW units
- `Money`, `Percentage`: Supporting value types
- **JSON/YAML Serialization**: Custom converters for type-safe configuration loading
  - `PowerJsonConverter`, `EnergyJsonConverter`, `PercentageJsonConverter`
  - Unit types serialize as structured objects: `{"amount": 2.5, "unit": "K_W"}`
  - Percentage serializes as fraction value (0.0-1.0)

**Configuration System** (`devices/src/main/java/de/hatoka/eos/devices/capi/business/config/`)
- `DeviceConfig`: Device configuration with direct field access
- `CarUsageProfile`: Record for electric car usage patterns
- `ChargingConfig`: Charging limits and behavior configuration
- All devices use `DeviceConfig` directly without persistence layer abstraction

### Technical Constraints

- **Java 21** with modern language features (records, pattern matching)
- **Quarkus framework** for dependency injection and testing
- **Gradle with Kotlin DSL** for build management
- **JaCoCo** for code coverage reporting

### Development Patterns

- **Record types**: Used extensively for immutable value objects (Energy, Power, etc.)
- **Builder pattern**: DeviceBuilder for complex device setup
- **Enum-based factories**: DeviceType enum creates device instances via reflection
- **Simulation priority**: Devices processed in enum ordinal order (SOLAR_PANEL → NOISY_USAGE → BATTERY → ELECTRIC_CAR → GRID)

### Testing Strategy

- **Quarkus Test framework** (`@QuarkusTest`) with CDI injection
- **JUnit 5** with Jupiter assertions
- Test location: `devices/src/test/java/`
- Code coverage with JaCoCo (XML and HTML reports)
- **Integration testing**: Prefer testing JSON/YAML mapping through `InstallationConfigLoaderTest` over isolated unit tests

## Type Safety and Configuration

### JSON/YAML Mapping Best Practices

**Custom Converters for Unit Types**:
- Always create combined `JsonConverter` classes with nested `Serializer` and `Deserializer` static classes
- Keep serialization/deserialization logic together for visibility and maintainability
- Use `@JsonSerialize` and `@JsonDeserialize` annotations directly on record types

**Configuration Type Safety**:
- Use proper unit types (`Power`, `Energy`, `Percentage`) in configuration classes instead of `Double`
- Eliminate manual conversions in builder/loader classes by having correct types from the start
- Update test YAML files to use structured unit format: `{"amount": 2.5, "unit": "K_W"}`
- Add `@JsonPropertyDescription` annotations to all configuration fields for API documentation

**CDI Integration**:
- Add `@Singleton` annotations to configuration loaders to enable dependency injection
- Ensure all injected dependencies have proper CDI annotations

**Testing JSON/YAML Mapping**:
- Test type mapping through integration tests that load actual configuration files
- Verify both the deserialized object structure AND the unit values/types
- Example: Test that `getProduction().amount()` returns expected value AND `getProduction().unit().name()` returns correct unit

## Electric Car Implementation

### Overview
The `ElectricCar` device extends the `Battery` class to provide vehicle-specific charging and discharging behavior with manual control and optimization capabilities.

### Key Implementation Details

**Architecture Pattern**:
- **Inheritance**: `ElectricCar extends Battery` - reuses proven battery simulation logic
- **Method Overrides**: Customizes behavior through `shouldChargeFromGrid()` and `shouldDischarge()` methods
- **DeviceType**: `ELECTRIC_CAR` enum entry with ElectricCar.class mapping

**Manual Charge Control**:
- `shouldChargeFromGrid(ZonedDateTime time, Percentage chargingLimit, DeviceState deviceState)` - controls when car charges from grid
- Default implementation: charges between 11 AM and 4 PM when below charging limit (`time.getHour() >= 11 && time.getHour() <= 16`)
- Override this method to implement custom charging schedules
- `getChargingLimit(SimulationStep step)` - returns `carChargingLimit` from configuration (vs. `forceChargingLimit` for batteries)

**Usage Profile System**:
- `CarUsageProfile` record defines when car is away from home and unavailable for charging
- Contains `List<DayOfWeek> days`, `LocalTime startUsage`, `LocalTime endUsage`, and `Energy energyConsumption`
- Car availability logic: available when `currentTime.isBefore(startUsage) || !currentTime.isBefore(endUsage)`
- Energy consumption applied when car returns from usage period
- Configuration via `DeviceConfig.setUsageProfile(CarUsageProfile)`

**V2G (Vehicle-to-Grid) Support**:
- `shouldDischarge()` - controls whether car can discharge back to grid
- Default implementation: returns `false` (no discharge)
- Override to `true` to enable V2G functionality

**Configuration Format**:
```yaml
- type: ELECTRIC_CAR
  capacity:
    amount: 50.0
    unit: "K_WH"
  chargeRate:
    amount: 11.0
    unit: "K_W"
  dischargeRate:
    amount: 10.0
    unit: "K_W"
  count: 1
  chargingEfficiency: 0.90        # 90% charging efficiency
  dischargingEfficiency: 0.90     # 90% discharging efficiency
  dailyStorageLoss: 0.05          # 5% daily storage loss
  usageProfile:                   # Optional usage profile
    days: [MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY]
    startUsage: "08:00"
    endUsage: "17:00"
    energyConsumption:
      amount: 15.0
      unit: "K_WH"

# Charging configuration
charging:
  forceChargingLimit: 0.1    # Battery charging limit (10%)
  carChargingLimit: 0.9      # Electric car charging limit (90%)
```

**Test Coverage**:
- `ElectricCarTest` provides comprehensive unit test coverage with detailed JavaDoc documentation
- Tests usage profile scenarios: weekend availability, work hours unavailability, energy consumption on return
- Tests charging behavior: excess energy charging, grid charging windows, efficiency calculations
- All test methods document expected behavior and calculation steps
- Integration tests verify system-wide energy balance impact

**Usage Patterns**:
- **Usage Profiles**: Configure when car is away and unavailable for charging with energy consumption
- **Smart Charging**: Override `shouldChargeFromGrid()` for time-of-use optimization
- **V2G Applications**: Override `shouldDischarge()` for grid stabilization services
- **Fleet Management**: Configure multiple cars with different charging schedules and usage patterns

## Battery Efficiency Implementation

### Overview
The battery system now supports realistic energy losses through configurable efficiency settings and daily storage losses.

### Key Features

**Charging and Discharging Efficiency**:
- `chargingEfficiency`: Energy loss during charging process (default: 90%)
- `dischargingEfficiency`: Energy loss during discharging process (default: 90%)
- Efficiencies are applied to all energy storage devices (Battery, ElectricCar)

**Daily Storage Loss**:
- `dailyStorageLoss`: Percentage of stored energy lost per day due to self-discharge (default: 5%)
- Applied continuously during simulation based on time duration
- Simulates real-world battery degradation and standby power consumption

**Configuration**:
```yaml
- type: BATTERY
  capacity:
    amount: 10.0
    unit: "K_WH"
  chargingEfficiency: 0.92        # 92% charging efficiency
  dischargingEfficiency: 0.95     # 95% discharging efficiency
  dailyStorageLoss: 0.03          # 3% daily storage loss
```

**Implementation Details**:
- Efficiency losses are calculated during energy transfer operations
- Storage losses are applied proportionally based on simulation step duration
- Both electric cars and batteries use the same efficiency system
- Default values reflect typical lithium-ion battery performance

## Solar Panel Efficiency Implementation

### Overview
Solar panels now support inverter efficiency to simulate real-world energy conversion losses from DC to AC power.

### Key Features

**Inverter Efficiency**:
- `panelEfficiency`: Energy conversion efficiency from DC to AC (default: 90%)
- Applied to all solar panel energy production alongside weather and statistics factors
- Simulates typical inverter losses in photovoltaic systems

**Configuration**:
```yaml
- type: SOLAR_PANEL
  production:
    amount: 5.0
    unit: "K_W"
  panelEfficiency: 0.92        # 92% inverter efficiency
  statisticsResource: "solar-statistics.yaml"
```

**Implementation Details**:
- Panel efficiency is multiplied with sun factor and statistics efficiency
- Applied during energy production calculation: `production × sunFactor × statisticsEfficiency × panelEfficiency`
- Default 90% efficiency reflects typical residential inverter performance
- Separate from weather-based efficiency variations captured in statistics

## Grid Configuration System

### Overview
The grid configuration system provides flexible energy pricing through multiple provider types. It supports both flat pricing models and CSV-based dynamic pricing with fallback configurations.

### Key Components

**GridConfig** (`devices/src/main/java/de/hatoka/eos/devices/capi/business/config/GridConfig.java`):
- Record-based configuration supporting multiple pricing providers
- `csvPriceProvider`: CSV-based dynamic pricing configuration
- `flatPriceProvider`: Simple flat rate pricing configuration
- Factory method `getEnergyPriceProvider()` creates appropriate `EnergyPriceForecast` implementation

**CsvPriceConfig** (`devices/src/main/java/de/hatoka/eos/devices/capi/business/config/CsvPriceConfig.java`):
- Record for CSV-based pricing configuration
- `currency`: Currency code for CSV price data (e.g., EUR, USD)
- `exportCharge`: Export charge configuration (price per energy unit)
- `importCharge`: Import charge configuration (price per energy unit)
- `resource`: List of CSV resource files containing time-series price data

**FlatPriceConfig** (`devices/src/main/java/de/hatoka/eos/devices/capi/business/config/FlatPriceConfig.java`):
- Record for flat rate pricing configuration
- `importPrice`: Fixed price per kWh when importing energy from grid
- `exportPrice`: Fixed price per kWh when exporting energy to grid
- Predefined constants: `NO_PRICING`, `GERMAN_RESIDENTIAL`

**ImportExportChargeConfig** (`devices/src/main/java/de/hatoka/eos/devices/capi/business/config/ImportExportChargeConfig.java`):
- Record defining price per energy unit structure
- `price`: Money amount with currency
- `energy`: Energy amount with unit

### Configuration Examples

**Flat Rate Pricing**:
```yaml
grid:
  flatPriceProvider:
    importPrice:
      amount: 0.39
      currency: EUR
    exportPrice:
      amount: 0.08
      currency: EUR
```

**CSV-Based Dynamic Pricing**:
```yaml
grid:
  csvPriceProvider:
    currency: EUR
    exportCharge:
      price:
        amount: 0.04
        currency: EUR
      energy:
        amount: 1
        unit: "K_WH"
    importCharge:
      price:
        amount: 0.08
        currency: EUR
      energy:
        amount: 1
        unit: "K_WH"
    resource:
      - stockprices_DE_LU_week_32-33.csv
```

### Implementation Details

**Provider Creation**:
- `GridConfig.getEnergyPriceProvider()` creates appropriate provider based on configuration
- CSV provider: Creates `CsvStockEnergyPriceProvider` when `csvPriceProvider` is configured
- Flat provider: Creates `FlatPriceService` when `flatPriceProvider` is configured
- Validation: Throws `IllegalStateException` if no provider is configured

**CSV Price Provider** (`CsvStockEnergyPriceProvider`):
- Loads CSV resources via ClassLoader at initialization
- Implements time-based price lookup from CSV data with hourly granularity
- CSV format: Column 1 = date (YYYY/MM/DD), Columns 2-25 = hourly prices (hours 0-23)
- Currency-aware: Uses configured currency instead of hardcoded EUR
- Price calculation:
  - **Import prices**: Market price (EUR/MWh → EUR/kWh) + import charges
  - **Export prices**: Market price (EUR/MWh → EUR/kWh) - export charges (minimum 0)
- Efficient storage: `Map<String, Map<Integer, Double>>` (date → hour → price)

**Testing**:
- `CsvPriceConfigTest`: Tests configuration loading and provider creation  
- Validates correct parsing of YAML configuration into record structures
- Tests actual price retrieval for specific date/time from CSV data
- Verifies currency configuration is properly applied to returned Money objects

### Architecture Benefits
- **Type Safety**: Record-based configuration with compile-time validation
- **Flexibility**: Support for multiple pricing models in single configuration system
- **Extensibility**: Easy to add new pricing providers by extending `EnergyPriceForecast`
- **Resource Management**: ClassLoader-based CSV resource loading with proper error handling
