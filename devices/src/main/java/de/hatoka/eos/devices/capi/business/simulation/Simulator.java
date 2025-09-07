package de.hatoka.eos.devices.capi.business.simulation;

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
}
