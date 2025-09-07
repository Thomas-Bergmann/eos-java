package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStep;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.devices.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class GridTest
{
    private static final ZonedDateTime startDate = DateTooling.SOMMER_NIGHT;

    private Grid createGrid()
    {
        return new Grid();
    }

    @Test
    public void testSimulateWithPositiveSystemEnergy_PowerGenerated()
    {
        Grid grid = createGrid();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(3.0)); // 3 kWh excess

        // Act
        SimulationStepResult result = grid.simulate(step, system, deviceState);

        // Assert - Grid should export excess energy
        assertNotNull(result);
        assertEquals(Energy.ofKwh(3.0), result.system().exported());
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithNegativeSystemEnergy_PowerNeeded()
    {
        Grid grid = createGrid();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT.consume(Energy.ofKwh(2.5)); // 2.5 kWh deficit

        // Act
        SimulationStepResult result = grid.simulate(step, system, deviceState);

        // Assert - Grid should import needed energy
        assertNotNull(result);
        assertEquals(Energy.ofKwh(2.5), result.system().imported());
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithZeroSystemEnergy_NoTransfer()
    {
        Grid grid = createGrid();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT; // Balanced system

        // Act
        SimulationStepResult result = grid.simulate(step, system, deviceState);

        // Assert - No energy transfer needed
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().imported());
        assertEquals(Energy.ZERO, result.system().exported());
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());
    }

    @Test
    public void testSimulateWithPricingExportEnergy()
    {
        Grid grid = createGrid();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(2.0)); // 2 kWh excess

        // Act
        SimulationStepResult result = grid.simulate(step, system, deviceState);

        // Assert - Grid exports 2 kWh and receives revenue
        assertNotNull(result);
        assertEquals(Energy.ofKwh(2.0), result.system().exported());
        assertEquals(Energy.ZERO, result.system().imported()); // We don't import from grid
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());

        // Revenue: 2 kWh * 0.08 EUR/kWh = 0.16 EUR (received from grid)
        assertEquals(Money.ZERO, result.system().importRevenue()); // We don't pay
        assertEquals(Money.ofEur(0.16), result.system().exportRevenue()); // We receive 0.16 EUR
    }

    @Test
    public void testSimulateWithPricingImportEnergy()
    {
        Grid grid = createGrid();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));
        DeviceState deviceState = DeviceState.NO_STORAGE;
        EnergySystem system = EnergySystem.INIT.consume(Energy.ofKwh(1.5)); // 1.5 kWh deficit

        // Act
        SimulationStepResult result = grid.simulate(step, system, deviceState);

        // Assert - Grid imports 1.5 kWh and incurs cost
        assertNotNull(result);
        assertEquals(Energy.ofKwh(1.5), result.system().imported()); // We import 1.5 kWh from grid
        assertEquals(Energy.ZERO, result.system().exported()); // We don't export to grid
        assertEquals(DeviceState.NO_STORAGE, result.deviceState());

        // Cost: 1.5 kWh * 0.39 EUR/kWh = 0.585 EUR (paid to grid)
        assertEquals(Money.ofEur(-0.585), result.system().importRevenue()); // We pay 0.585 EUR
        assertEquals(Money.ZERO, result.system().exportRevenue()); // We don't receive money
    }
}