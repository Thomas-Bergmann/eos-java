package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.optimization.capi.business.OptimizationGoal;

public class GridUsingGoal implements OptimizationGoal
{
    @JsonProperty("active")
    private final boolean isActivated = true;

    @Override
    public Money getPenalty(SimulationResult simulationResult)
    {
        return simulationResult.system().getEnergyRevenue().negate();
    }
}
