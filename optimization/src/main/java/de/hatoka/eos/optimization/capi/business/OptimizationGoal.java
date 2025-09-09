package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Money;

public interface OptimizationGoal
{
    /**
     * @param simulationResult result of simulation
     * @return the penalty as money (less is better)
     */
    Money getPenalty(SimulationResult simulationResult);
}
