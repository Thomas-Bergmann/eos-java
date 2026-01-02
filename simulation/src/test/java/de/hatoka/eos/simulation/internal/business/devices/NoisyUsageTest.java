package de.hatoka.eos.simulation.internal.business.devices;

import de.hatoka.eos.simulation.capi.business.config.DeviceConfig;
import de.hatoka.eos.simulation.capi.business.device.DeviceState;
import de.hatoka.eos.simulation.capi.business.simulation.EnergySystem;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStep;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Power;
import de.hatoka.eos.simulation.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class NoisyUsageTest
{
    private static final ZonedDateTime startDate = DateTooling.SOMMER_NIGHT;

    private DeviceConfig getStandardUsage(double consumption)
    {
        DeviceConfig config = new DeviceConfig();
        config.setConsumption(Power.ofKw(consumption));
        return config;
    }

    @Test
    public void testSimulateWithHalfHourDuration()
    {
        // Arrange
        NoisyUsage noisyUsage = new NoisyUsage(getStandardUsage(3));

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofMinutes(30));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT;

        // Act
        SimulationStepResult result = noisyUsage.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);
        assertEquals(Energy.ofKwh(1.5), result.system().consumed()); // 3.0 kW * 0.5 hour = 1.5 kWh consumed
        assertEquals(Energy.ZERO, result.system().produced());
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithZeroConsumption()
    {
        // Arrange
        NoisyUsage noisyUsage = new NoisyUsage(getStandardUsage(0));

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(2));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT;

        // Act
        SimulationStepResult result = noisyUsage.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().consumed());
        assertEquals(Energy.ZERO, result.system().produced());
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

}