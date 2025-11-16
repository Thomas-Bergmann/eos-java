package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.units.capi.Money;

/**
 * OptimizationGoal defines a goal of optimization, each goal is represented by a penalty if the goal was not reached.
 */
public interface OptimizationGoal
{
    /**
     * @param simulationResult result of simulation
     * @return the penalty as money (less is better)
     */
    Money getPenalty(SimulationResult simulationResult);
}
