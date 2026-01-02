package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.units.capi.Money;

/**
 * OptimizationResult contains the simulation result and the calculated penalty for optimization goals.
 * @param simulationResult result of simulation
 * @param penalty penalty for non reached goals
 */
public record OptimizationResult(SimulationResult simulationResult, Money penalty)
{
    public Money getPenalty()
    {
        return penalty;
    }
}
