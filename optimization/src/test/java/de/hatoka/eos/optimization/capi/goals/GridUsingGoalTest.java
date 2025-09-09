package de.hatoka.eos.optimization.capi.goals;

import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Money;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GridUsingGoalTest
{
    private final GridUsingGoal goal = new GridUsingGoal();

    @Test
    public void testPenaltyWithPositiveEnergyRevenue()
    {
        SimulationResult simulationResult = new SimulationResult(null, // request - not needed for this test
                        null, // step - not needed for this test
                        Map.of(), // endState - not needed for this test
                        EnergySystem.INIT.importEnergy(Energy.ofKwh(10.0), Money.ofEur(2.40)).exportEnergy(Energy.ofKwh(10.0), Money.ofEur(1.40)));
        assertEquals(Money.ofEur(1), goal.getPenalty(simulationResult));
    }
}
