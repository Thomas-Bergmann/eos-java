package de.hatoka.eos.devices.capi.business.device;

import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStep;

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