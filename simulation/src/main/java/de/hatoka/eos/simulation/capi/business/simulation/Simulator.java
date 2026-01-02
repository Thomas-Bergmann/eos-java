package de.hatoka.eos.simulation.capi.business.simulation;

import java.util.List;

/**
 * Simulator can execute multiple simulations
 */
public interface Simulator
{
    /**
     * Execute a simulation
     * @param request configuration of simulation
     * @return simulation result
     */
    SimulationResult simulate(SimulationRequest request);

    /**
     * Execute a simulation with device manipulators
     * @param request configuration of simulation
     * @param manipulators device manipulators
     * @return simulation result
     */
    SimulationResult simulate(SimulationRequest request, List<DeviceManipulator> manipulators);
}
