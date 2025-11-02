package de.hatoka.eos.optimization.capi.goals;

import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Money;
import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.optimization.internal.business.config.OptimizationConfigurationLoader;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test calculation of car goals.
 */
@QuarkusTest
public class CarChargeGoalTest
{
    @Inject
    private OptimizationConfigurationLoader loader;

    @Test
    public void testPenaltyCalculationWhenBatteryIs60Percent() throws IOException
    {
        OptimizationGoals goals = loader.loadGoals("goal-for-optimization.yaml");
        DeviceRef carRef = new DeviceRef(DeviceType.ELECTRIC_CAR, "car1");
        DeviceState carState = new DeviceState(
                        Energy.ofKwh(50.0),    // 50 kWh max capacity
                        new Percentage(0.6)     // 60% charged
        );
        // Set goal to reach 80% charge using reflection since fields are private
        Money actualPenalty = goals.getPenalty(new SimulationResult(null, null, Map.of(carRef, carState), EnergySystem.INIT));

        // Shortfall: 90% - 60% = 30% -> 3 x 5 EUR = 15 EUR
        assertEquals(Money.ofEur(15), actualPenalty);
    }
}
