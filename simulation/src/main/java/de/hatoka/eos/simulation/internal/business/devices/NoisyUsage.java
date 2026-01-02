package de.hatoka.eos.simulation.internal.business.devices;

import de.hatoka.eos.simulation.capi.business.config.DeviceConfig;
import de.hatoka.eos.simulation.capi.business.device.Device;
import de.hatoka.eos.simulation.capi.business.device.DeviceState;
import de.hatoka.eos.simulation.capi.business.simulation.EnergySystem;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStep;
import de.hatoka.eos.units.capi.Power;

import java.time.ZonedDateTime;

/**
 * Represents a noisy power consumer, which consumes power but does not produce or store it.
 */
public class NoisyUsage implements Device
{
    private final DeviceConfig config;

    public NoisyUsage(DeviceConfig config)
    {
        this.config = config;
    }

    @Override
    public SimulationStepResult simulate(SimulationStep step, EnergySystem system, DeviceState deviceState)
    {
        return SimulationStepResult.build(system.consume(getConsumption(step.startDate()).multiply(step.duration())));
    }

    /**
     * Calculates/Predicts the power consumption for the given date.
     * @param date date and time of the consumption
     * @return average power consumption at the given date
     */
    private Power getConsumption(ZonedDateTime date)
    {
        return config.getConsumption();
    }
}
