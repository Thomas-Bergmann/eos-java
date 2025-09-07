package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStep;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Percentage;
import de.hatoka.eos.devices.capi.units.Power;
import de.hatoka.eos.devices.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SolarPanelTest
{
    private DeviceConfig getStandardPanelConfig(double production)
    {
        DeviceConfig config = new DeviceConfig();
        config.setProduction(Power.ofKw(production));
        config.setPanelEfficiency(Percentage.ONE_HUNDRED); // 100% efficiency for test
        return config;
    }

    @Test
    public void testSimulateGeneratesPowerBasedOnProductionAndDuration()
    {
        // Arrange
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(2.5));

        SimulationStep step = SimulationStep.valueOf(DateTooling.SOMMER_SUN, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT;

        // Act
        SimulationStepResult result = solarPanel.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);
        assertEquals(Energy.ofKwh(2.5), result.system().produced()); // 2.5 kW * 1 hour = 2.5 kWh
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithHalfHourDuration()
    {
        // Arrange
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(4));

        SimulationStep step = SimulationStep.valueOf(DateTooling.SOMMER_SUN, Duration.ofMinutes(30));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT;

        // Act
        SimulationStepResult result = solarPanel.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);
        assertEquals(Energy.ofKwh(2.0), result.system().produced()); // 4.0 kW * 0.5 hour = 2.0 kWh
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithZeroPowerProduction()
    {
        // Arrange
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(0));

        SimulationStep step = SimulationStep.valueOf(DateTooling.SOMMER_NIGHT, Duration.ofHours(2));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT;

        // Act
        SimulationStepResult result = solarPanel.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().produced());
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithNoSunlight()
    {
        // Arrange
        SolarPanel solarPanel = new SolarPanel(getStandardPanelConfig(5));

        SimulationStep step = SimulationStep.valueOf(DateTooling.SOMMER_NIGHT, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT;

        // Act
        SimulationStepResult result = solarPanel.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().produced()); // No production due to weather
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithDefaultPanelEfficiency()
    {
        // Arrange - Test default 90% panel efficiency when not explicitly set
        DeviceConfig config = new DeviceConfig();
        config.setProduction(Power.ofKw(4.0)); // 4.0 kW max
        config.setPanelEfficiency(new Percentage(0.9)); // 90% (is default - but explicitly set for clarity)

        SolarPanel solarPanel = new SolarPanel(config);

        SimulationStep step = SimulationStep.valueOf(DateTooling.SOMMER_SUN, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT;

        // Act
        SimulationStepResult result = solarPanel.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);
        assertEquals(Energy.ofKwh(3.6), result.system().produced()); // 4.0 kW * 1 hour * 0.9 default efficiency = 3.6 kWh
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }
}