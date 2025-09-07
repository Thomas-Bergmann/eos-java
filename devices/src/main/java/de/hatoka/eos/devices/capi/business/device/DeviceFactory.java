package de.hatoka.eos.devices.capi.business.device;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;

public interface DeviceFactory
{
    Device createDevice(DeviceConfig config);
}