package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.units.capi.Money;
import de.hatoka.eos.optimization.capi.business.OptimizationGoal;

/**
 * The GridUsingGoal is the spend amount of money with the grid. This goal is activated by default, but can be deactivated.
 * <li>active - used or not (default true)</li>
 */
public class GridUsingGoal implements OptimizationGoal
{
    @JsonProperty("active")
    private final boolean isActivated = true;

    @Override
    public Money getPenalty(SimulationResult simulationResult)
    {
        return isActivated ? simulationResult.system().getEnergyRevenue().negate() : Money.ZERO;
    }
}
