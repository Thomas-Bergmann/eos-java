package de.hatoka.eos.simulation.capi.business.simulation;

import de.hatoka.eos.simulation.capi.business.device.Device;
import de.hatoka.eos.simulation.capi.business.device.DeviceRef;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;

/**
 * DeviceManipulator can change the configuration of devices
 */
public interface DeviceManipulator
{
    /**
     * Changes the configuration of a device temporarily.
     * @param time time of simulation
     * @param devices existing devices
     * @return manipulated devices
     */
    Map<DeviceRef, Device> apply(ZonedDateTime time, Map<DeviceRef, Device> devices);

    /**
     * @return a list of neighbors
     */
    List<DeviceManipulator> evolute();
}
