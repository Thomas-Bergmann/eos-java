package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.optimization.capi.business.OptimizationGoal;

/**
 * OptimizationGoals is the container for all OptimizationGoals. All goals are explicitly named to provide a strict/readable structure of goals.
 */
public class OptimizationGoals implements OptimizationGoal
{
    /**
     * Grid usage is mostly mandatory goal, can be deactivated.
     */
    private final GridUsingGoal gridUsingGoal = new GridUsingGoal();

    @JsonProperty("carCharging")
    private CarChargeGoal carCharging;

    /**
     * @param simulationResult result of simulation
     * @return the sum of all penalties
     */
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
