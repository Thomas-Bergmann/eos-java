package de.hatoka.eos.simulation.capi.business.device;

import de.hatoka.eos.simulation.capi.business.simulation.EnergySystem;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStep;

/**
 * Business object for import job management focused on the importing behavior
 */
public interface Device
{
    SimulationStepResult simulate(SimulationStep step, EnergySystem system, DeviceState deviceState);

    default DeviceState getInitialState()
    {
        return DeviceState.NO_STORAGE;
    }
}