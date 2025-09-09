package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.optimization.capi.business.OptimizationGoal;

public class OptimizationGoals implements OptimizationGoal
{
    /**
     * Grid usage is mandatory goal
     */
    private final GridUsingGoal gridUsingGoal = new GridUsingGoal();

    @JsonProperty("carCharging")
    private CarChargeGoal carCharging;

    @Override
    public Money getPenalty(SimulationResult simulationResult)
    {
        return carCharging.getPenalty(simulationResult).add(gridUsingGoal.getPenalty(simulationResult));
    }

    public CarChargeGoal getCarCharging()
    {
        return carCharging;
    }
}
