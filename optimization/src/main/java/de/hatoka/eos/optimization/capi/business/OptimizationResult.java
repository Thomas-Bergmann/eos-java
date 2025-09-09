package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Money;

public record OptimizationResult(SimulationResult simulationResult, Money penalty)
{
    public Money getPenalty()
    {
        return penalty;
    }
}
