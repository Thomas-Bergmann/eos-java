package de.hatoka.eos.optimization.capi.goals;

import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.devices.capi.units.Percentage;
import de.hatoka.eos.optimization.internal.business.config.OptimizationConfigurationLoader;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test the accumulation over all optimization goals, simply without doing an optimization.
 */
@QuarkusTest
public class OptimizationGoalsTest
{
    @Inject
    private OptimizationConfigurationLoader optimizationConfigurationLoader;

    @Test
    public void testPenaltyCalculationWhenBatteryIs60Percent() throws IOException
    {
        OptimizationGoals goals = optimizationConfigurationLoader.loadGoals("goal-for-optimization.yaml");
        DeviceRef carRef = new DeviceRef(DeviceType.ELECTRIC_CAR, "car1");
        DeviceState carState = new DeviceState(Energy.ofKwh(50.0),    // 50 kWh max capacity
                        new Percentage(0.6)     // 60% charged
        );
        // Set goal to reach 80% charge using reflection since fields are private
        Money actualPenalty = goals.getPenalty(new SimulationResult(null, null, Map.of(carRef, carState),
                        EnergySystem.INIT.importEnergy(Energy.ofKwh(10.0), Money.ofEur(2.40)).exportEnergy(Energy.ofKwh(10.0), Money.ofEur(1.40))));

        // Shortfall: 90% - 60% = 30% -> 3 x 5 EUR = 15 EUR + 1EUR (for grid difference)
        assertEquals(Money.ofEur(16), actualPenalty);
    }
}
