package de.hatoka.eos.simulation.capi.business.device;

import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Percentage;

/**
 * DeviceState represents the state of a device in a simulation context.
 *
 * @param maxEnergy max stored energy in the device
 */
public record DeviceState(Energy maxEnergy, Percentage percentage)
{
    public static final DeviceState NO_STORAGE = DeviceState.empty(Energy.ZERO);

    public static DeviceState empty(Energy maxEnergy)
    {
        return new DeviceState(maxEnergy, Percentage.ZERO);
    }

    public Energy storedEnergy()
    {
        return maxEnergy.multiply(percentage);
    }

    public DeviceState apply(Energy newStoredEnergy)
    {
        return new DeviceState(maxEnergy, newStoredEnergy.percentageOf(maxEnergy));
    }
}
