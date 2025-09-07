package de.hatoka.eos.devices.capi.business.simulation;

import de.hatoka.eos.devices.capi.business.device.DeviceState;

/**
 * SimulationStepResult is the result of a simulation step of a device
 * @param system energy system state after simulation
 * @param deviceState device state after simulation
 */
public record SimulationStepResult(EnergySystem system, DeviceState deviceState)
{
    /**
     * Create step result for devices with own state
     * @param system end state of system
     * @param newDeviceState end state of device
     * @return step result
     */
    public static SimulationStepResult build(EnergySystem system, DeviceState newDeviceState)
    {
        return new SimulationStepResult(system, newDeviceState);
    }

    /**
     * Create step result for devices without own state
     * @param system end state of system
     * @return step result
     */
    public static SimulationStepResult build(EnergySystem system)
    {
        return new SimulationStepResult(system, DeviceState.NO_STORAGE);
    }
}
