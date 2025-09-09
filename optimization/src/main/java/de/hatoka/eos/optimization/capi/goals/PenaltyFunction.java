package de.hatoka.eos.optimization.capi.goals;

import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Money;

/**
 *
 */
public interface PenaltyFunction
{
    Money getPenalty(SimulationResult simulationResult);
}
