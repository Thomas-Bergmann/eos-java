package de.hatoka.eos.devices.capi.business.device;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public interface DeviceFactory
{
    Device createDevice(DeviceConfig config);

    Map<DeviceRef, Device> createDevices(List<DeviceConfig> configs);

    default Map<DeviceRef, DeviceState> createInitialState(List<DeviceConfig> configs)
    {
        Map<DeviceRef, Device> devices = createDevices(configs);
        Map<DeviceRef, DeviceState> states = new HashMap<>();
        devices.forEach((k, d) -> states.put(k, d.getInitialState()));
        return states;
    }
}