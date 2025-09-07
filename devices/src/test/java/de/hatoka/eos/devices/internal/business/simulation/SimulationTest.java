package de.hatoka.eos.devices.internal.business.simulation;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.capi.business.forecast.Forecasts;
import de.hatoka.eos.devices.capi.business.simulation.SimulationRequest;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.business.simulation.Simulator;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.devices.capi.units.Percentage;
import de.hatoka.eos.devices.capi.units.Power;
import de.hatoka.eos.devices.internal.business.DateTooling;
import de.hatoka.eos.devices.internal.business.config.ConfigurationDeviceBuilder;
import de.hatoka.eos.devices.internal.business.config.ConfigurationLoader;
import de.hatoka.eos.devices.internal.business.devices.Battery;
import de.hatoka.eos.devices.internal.business.devices.Grid;
import de.hatoka.eos.devices.internal.business.devices.NoisyUsage;
import de.hatoka.eos.devices.internal.business.devices.SolarPanel;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class SimulationTest
{
    @Inject
    private ConfigurationDeviceBuilder configurationDeviceBuilder;
    @Inject
    private ConfigurationLoader configurationLoader;
    @Inject
    private Simulator simulator;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationTest.class);
    private static final ZonedDateTime MID_NIGHT_START = DateTooling.SOMMER_NIGHT;
    private static final ZonedDateTime MID_NIGHT_END = MID_NIGHT_START.plusHours(1);
    private static final ZonedDateTime MID_SUN_START = DateTooling.SOMMER_SUN;
    private static final ZonedDateTime MID_SUN_END = MID_SUN_START.plusHours(1);

    private static DeviceState createStandardDeviceState(Percentage percentage)
    {
        return new DeviceState(Energy.ofKwh(10.0), percentage); // 10 kWh capacity
    }

    private Grid createGrid()
    {
        return new Grid();
    }

    private DeviceConfig getStandardBatteryConfig()
    {
        DeviceConfig config = new DeviceConfig();
        config.setCapacity(Energy.ofKwh(10.0)); // 10 kWh capacity
        config.setChargeRate(Power.ofKw(5.0)); // 5 kW charge rate
        config.setDischargeRate(Power.ofKw(4.0)); // 4 kW discharge rate
        // Set 100% efficiency and 0% storage loss for tests
        config.setChargingEfficiency(Percentage.ONE_HUNDRED);
        config.setDischargingEfficiency(Percentage.ONE_HUNDRED);
        config.setDailyStorageLoss(Percentage.ZERO);
        return config;
    }

    private DeviceConfig getStandardPanelConfig(double production)
    {
        DeviceConfig config = new DeviceConfig();
        config.setProduction(Power.ofKw(production));
        config.setPanelEfficiency(Percentage.ONE_HUNDRED); // 100% efficiency for test
        return config;
    }

    private DeviceConfig getStandardUsage(double consumption)
    {
        DeviceConfig config = new DeviceConfig();
        config.setConsumption(Power.ofKw(consumption));
        return config;
    }

    @Test
    public void testSimulationWithBatteryAndGrid()
    {
        // Arrange - Battery (50% charged) and Grid for 1 hour
        DeviceRef batteryRef = new DeviceRef(DeviceType.BATTERY, "test-battery");
        Battery battery = new Battery(getStandardBatteryConfig());

        DeviceRef gridRef = new DeviceRef(DeviceType.GRID, "test-grid");
        Grid grid = createGrid();

        Map<DeviceRef, Device> devices = new HashMap<>();
        devices.put(batteryRef, battery);
        devices.put(gridRef, grid);

        Map<DeviceRef, DeviceState> initialState = new HashMap<>();
        initialState.put(batteryRef, createStandardDeviceState(new Percentage(0.5))); // 50% charged

        SimulationRequest request = new SimulationRequest("test-sim", MID_NIGHT_START, MID_NIGHT_END, Duration.ofHours(1), devices, initialState,
                        Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);

        // Assert - With no energy input/output, battery and grid should remain unchanged
        assertNotNull(result);
        assertEquals(Energy.ofKwh(5.0), result.endState().get(batteryRef).storedEnergy()); // Battery unchanged
        assertEquals(0.5, result.endState().get(batteryRef).percentage().value(), 0.001); // 50% unchanged
        assertEquals(DeviceState.NO_STORAGE, result.endState().get(gridRef)); // Grid unchanged
    }

    @Test
    public void testSimulationWithSolarPanelAndGrid()
    {
        // Arrange - Solar Panel (2 kW) and Grid for 1 hour
        DeviceRef solarRef = new DeviceRef(DeviceType.SOLAR_PANEL, "test-solar");
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(2));

        DeviceRef gridRef = new DeviceRef(DeviceType.GRID, "test-grid");
        Grid grid = createGrid();

        Map<DeviceRef, Device> devices = new HashMap<>();
        devices.put(solarRef, solarPanel);
        devices.put(gridRef, grid);

        SimulationRequest request = new SimulationRequest("test-sim", MID_NIGHT_START, MID_NIGHT_END, Duration.ofHours(1), devices, Collections.emptyMap(), Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);

        // Assert - Solar produces 2 kWh, Grid should export it (system has 2 kWh excess)
        assertNotNull(result);
        assertEquals(DeviceState.NO_STORAGE, result.endState().get(solarRef)); // Solar panel doesn't store energy
        assertEquals(DeviceState.NO_STORAGE, result.endState().get(gridRef)); // Grid doesn't store energy
        // Note: We can't directly observe the energy transfer in this simple test,
        // but the simulation should process: Solar produces 2 kWh → Grid exports 2 kWh
    }

    @Test
    public void testSimulationWithAllDevices()
    {
        // Arrange - Complete system: Solar Panel (3 kW) → NoisyUsage (1 kW) → Battery (50%) → Grid
        // Scenario: Solar produces 3 kWh, Usage consumes 1 kWh, Battery gets 2 kWh excess

        DeviceRef solarRef = new DeviceRef(DeviceType.SOLAR_PANEL, "solar-panel");
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(3));

        DeviceRef usageRef = new DeviceRef(DeviceType.NOISY_USAGE, "noisy-usage");
        NoisyUsage noisyUsage = new NoisyUsage(getStandardUsage(1));

        DeviceRef batteryRef = new DeviceRef(DeviceType.BATTERY, "battery");
        Battery battery = new Battery(getStandardBatteryConfig());

        DeviceRef gridRef = new DeviceRef(DeviceType.GRID, "grid");
        Grid grid = createGrid();

        Map<DeviceRef, Device> devices = new HashMap<>();
        devices.put(solarRef, solarPanel);
        devices.put(usageRef, noisyUsage);
        devices.put(batteryRef, battery);
        devices.put(gridRef, grid);

        Map<DeviceRef, DeviceState> initialState = new HashMap<>();
        initialState.put(batteryRef, createStandardDeviceState(new Percentage(0.5))); // 50% charged

        SimulationRequest request = new SimulationRequest("comprehensive-sim", MID_SUN_START, MID_SUN_END, Duration.ofHours(1), devices, initialState, Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);

        // Assert
        assertNotNull(result);

        // Solar Panel should remain empty (no stored energy)
        assertEquals(DeviceState.NO_STORAGE, result.endState().get(solarRef));

        // NoisyUsage should remain empty (no stored energy)
        assertEquals(DeviceState.NO_STORAGE, result.endState().get(usageRef));

        // Battery should have charged with excess energy
        // Solar produces 3 kWh, Usage consumes 1 kWh, Battery gets 2 kWh
        // Battery: 5.0 + 2.0 = 7.0 kWh (70%)
        assertEquals(Energy.ofKwh(7.0), result.endState().get(batteryRef).storedEnergy());
        assertEquals(0.7, result.endState().get(batteryRef).percentage().value(), 0.001);

        // Grid should remain empty (no stored energy, perfectly balanced)
        assertEquals(DeviceState.NO_STORAGE, result.endState().get(gridRef));

        // Grid transfers should show no activity (perfectly balanced system)
        assertNotNull(result.system());
        assertEquals(Energy.ZERO, result.system().imported());
        assertEquals(Energy.ZERO, result.system().exported());
        assertEquals(Money.ZERO, result.system().importRevenue());
        assertEquals(Money.ZERO, result.system().exportRevenue());

        // Test net result using getTransferCosts() - perfectly balanced, so no cost or revenue
        assertEquals(Money.ZERO, result.system().getEnergyRevenue());
    }

    @Test
    public void testSimulationWithExcessEnergyExport()
    {
        // Arrange - Solar Panel (3 kW) and Grid for 1 hour (excess energy should be exported)
        DeviceRef solarRef = new DeviceRef(DeviceType.SOLAR_PANEL, "test-solar-export");
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(3));

        DeviceRef gridRef = new DeviceRef(DeviceType.GRID, "test-grid-export");
        Grid grid = createGrid();

        Map<DeviceRef, Device> devices = new HashMap<>();
        devices.put(solarRef, solarPanel);
        devices.put(gridRef, grid);

        SimulationRequest request = new SimulationRequest("export-sim", MID_SUN_START, MID_SUN_END, Duration.ofHours(1), devices, Collections.emptyMap(), Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);

        // Assert
        assertNotNull(result);

        // Grid transfers should show export of 3 kWh with German residential pricing (8¢ per kWh = 0.24 EUR)
        assertNotNull(result.system());
        assertEquals(Energy.ZERO, result.system().imported()); // No import
        assertEquals(Energy.ofKwh(3.0), result.system().exported()); // Export 3 kWh
        assertEquals(Money.ZERO, result.system().importRevenue()); // No cost
        assertEquals(Money.ofEur(0.24), result.system().exportRevenue()); // Revenue: 3 * 0.08 = 0.24 EUR

        // Test net result using getTransferCosts() - should be positive revenue of 0.24 EUR
        assertEquals(Money.ofEur(0.24), result.system().getEnergyRevenue()); // Net profit from export (negative cost = profit)
    }

    @Test
    public void testSimulationWithMixedGridOperations()
    {
        // Arrange - Test mixed scenario: Solar (2 kW) + NoisyUsage (3 kW) = 1 kW deficit (import needed)
        DeviceRef solarRef = new DeviceRef(DeviceType.SOLAR_PANEL, "mixed-solar");
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(2));

        DeviceRef usageRef = new DeviceRef(DeviceType.NOISY_USAGE, "mixed-usage");
        NoisyUsage noisyUsage = new NoisyUsage(getStandardUsage(3));

        DeviceRef gridRef = new DeviceRef(DeviceType.GRID, "mixed-grid");
        Grid grid = createGrid();

        Map<DeviceRef, Device> devices = new HashMap<>();
        devices.put(solarRef, solarPanel);
        devices.put(usageRef, noisyUsage);
        devices.put(gridRef, grid);

        SimulationRequest request = new SimulationRequest("mixed-sim", MID_SUN_START, MID_SUN_END, Duration.ofHours(1), devices, Collections.emptyMap(), Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);

        // Assert
        assertNotNull(result);

        // Grid should import 1 kWh (3 kWh usage - 2 kWh solar = 1 kWh deficit)
        // German residential import price: 39¢ per kWh = 0.39 EUR cost
        assertNotNull(result.system());
        assertEquals(Energy.ofKwh(1.0), result.system().imported()); // Import 1 kWh
        assertEquals(Energy.ZERO, result.system().exported()); // No export
        assertEquals(Money.ofEur(-0.39), result.system().importRevenue()); // Cost: 1 * 0.39 = 0.39 EUR
        assertEquals(Money.ZERO, result.system().exportRevenue()); // No revenue

        // Test net result using getTransferCosts() - should be positive (cost) of 0.39 EUR
        // getTransferCosts() = importRevenue - exportRevenue = 0.39 - 0.00 = 0.39
        assertEquals(Money.ofEur(-0.39), result.system().getEnergyRevenue()); // Net loss from import
    }

    @Test
    public void testFullDaySimulationWithTestInstallationConfig() throws IOException
    {
        // Arrange - Load actual test-installation.yaml configuration
        InstallationConfig installationConfig = configurationLoader.load("test-installation-without-car.yaml");

        // Simulate one full day (24 hours) with 1-hour steps
        ZonedDateTime startDate = ZonedDateTime.of(2024, 6, 21, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")); // Summer solstice
        ZonedDateTime endDate = startDate.plusDays(1);
        Duration stepDuration = Duration.ofHours(1);
        
        SimulationRequest request = new SimulationRequest("full-day-test", startDate, endDate, stepDuration, configurationDeviceBuilder.getDevices(installationConfig), Collections.emptyMap(), Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);
        
        // Assert - Focus only on grid costs/revenues for the full day
        assertNotNull(result);
        assertNotNull(result.system());

        LOGGER.info("Net result: {}", result.system());

        // energy values
        assertEquals(25.94, result.system().produced().amount(), 0.05);
        assertEquals(1.74, result.system().discharged().amount(), 0.05);
        assertEquals(12.02, result.system().charged().amount(), 0.05);
        assertEquals(2.26, result.system().imported().amount(), 0.05);
        assertEquals(11.92, result.system().exported().amount(), 0.05);
        assertEquals(6, result.system().consumed().amount(), 0.05);
        // costs - Using Money.round() for 2-decimal precision comparison
        assertEquals(Money.ofEur(-0.88), result.system().importRevenue().round()); // Cost from grid import
        assertEquals(Money.ofEur(0.95), result.system().exportRevenue().round()); // Revenue from grid export
        assertEquals(Money.ofEur(0.07), result.system().getEnergyRevenue().round()); // Net profit from export (negative cost = profit)
    }

    @Test
    public void testWithCar() throws IOException
    {
        // Arrange - Load actual test-installation.yaml configuration
        InstallationConfig installationConfig = configurationLoader.load("test-installation-with-car.yaml");

        // Simulate one full day (24 hours) with 1-hour steps
        ZonedDateTime startDate = ZonedDateTime.of(2024, 6, 21, 0, 0, 0, 0, ZoneId.of("Europe/Berlin")); // Summer solstice
        ZonedDateTime endDate = startDate.plusDays(1);
        Duration stepDuration = Duration.ofHours(1);

        SimulationRequest request = new SimulationRequest("full-day-test", startDate, endDate, stepDuration, configurationDeviceBuilder.getDevices(installationConfig), Collections.emptyMap(), Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);

        // Assert - Focus only on grid costs/revenues for the full day
        assertNotNull(result);
        assertNotNull(result.system());

        // Print summary for debugging (can be removed in final version)
        LOGGER.info("Net result: {}", result.system());

        // energy values
        assertEquals(25.94, result.system().produced().amount(), 0.05);
        assertEquals(2.09, result.system().discharged().amount(), 0.05);
        assertEquals(58.36, result.system().charged().amount(), 0.05);
        assertEquals(37.53, result.system().imported().amount(), 0.05);
        assertEquals(0, result.system().exported().amount(), 0.05);
        assertEquals(7.2, result.system().consumed().amount(), 0.05);
        // costs - Using Money.round() for 2-decimal precision comparison
        assertEquals(Money.ofEur(-14.63), result.system().importRevenue().round()); // Cost from grid import
        assertEquals(Money.ZERO, result.system().exportRevenue().round()); // Revenue from grid export
        assertEquals(Money.ofEur(-14.63), result.system().getEnergyRevenue().round()); // Net profit from export (negative cost = profit)
    }

    @Test
    public void testSimulationWithBatteryCarAndUsageFrom100Percent() throws IOException
    {
        // Load configuration with battery, electric car, and noisy usage - all starting at 100%
        InstallationConfig config = configurationLoader.load("test-simulation-100percent.yaml");
        
        // Simulate for 1 hour at night (no solar production)
        SimulationRequest request = new SimulationRequest(
            "today-energy-simulation",
                        ZonedDateTime.now().minus(Duration.ofHours(24)),
                        ZonedDateTime.now().minus(Duration.ofHours(23)), // two points (incl. outer)
            Duration.ofHours(1), 
            configurationDeviceBuilder.getDevices(config), 
            Collections.emptyMap(),
            Forecasts.STANDARD
        );
        
        SimulationResult result = simulator.simulate(request);
        
        // Assert - System should discharge battery first to cover 2 kWh usage
        assertNotNull(result);
        assertNotNull(result.system());
        
        LOGGER.info("Battery and car at 100% with 2kW usage: {}", result.system());
        
        // Get device references (using correct naming from ConfigurationDeviceBuilder)
        DeviceRef batteryRef = new DeviceRef(DeviceType.BATTERY, "Battery");
        DeviceRef carRef = new DeviceRef(DeviceType.ELECTRIC_CAR, "ElectricCar");
        
        // Battery should discharge 2 kWh to cover noisy usage (10.0 - 2.0 = 8.0 kWh remaining, 80%)
        assertEquals(Energy.ofKwh(8.0), result.endState().get(batteryRef).storedEnergy());
        assertEquals(0.8, result.endState().get(batteryRef).percentage().value(), 0.001);
        
        // Electric car should remain unchanged at 100% (battery discharges first)
        assertEquals(Energy.ofKwh(50.0), result.endState().get(carRef).storedEnergy());
        assertEquals(1.0, result.endState().get(carRef).percentage().value(), 0.001);
        
        // Energy system should show 2 kWh consumption and 2 kWh discharge, no grid activity
        assertEquals(Energy.ofKwh(2.0), result.system().consumed());
        assertEquals(Energy.ofKwh(2.0), result.system().discharged());
        assertEquals(Energy.ZERO, result.system().imported());
        assertEquals(Energy.ZERO, result.system().exported());
        assertEquals(Money.ZERO, result.system().getEnergyRevenue());
    }
}