package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.units.capi.Money;

/**
 * OptimizationResult contains the simulation result and the calculated penalty for optimization goals.
 * @param penalty penalty for non reached goals
 * @param manipulators
 */
public record OptimizationResult(Money penalty,
                java.util.List<de.hatoka.eos.simulation.capi.business.simulation.DeviceManipulator> manipulators)
{
    public Money getPenalty()
    {
        return penalty;
    }

    public boolean isBetterThan(OptimizationResult other)
    {
        return getPenalty().isLessThan(other.getPenalty());
    }
}
