package de.hatoka.eos.devices.capi.business.simulation;

import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceState;

import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Summary of simulation with simulation configuration.
 * @param request simulation request
 * @param step results of step
 * @param endState device state at the end of the simulation
 * @param system system state at the end of the simulation
 */
public record SimulationResult(SimulationRequest request, SimulationStep step,  Map<DeviceRef, DeviceState> endState, EnergySystem system)
{
}
