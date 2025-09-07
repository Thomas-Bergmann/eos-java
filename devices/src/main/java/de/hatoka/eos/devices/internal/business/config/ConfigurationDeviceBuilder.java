package de.hatoka.eos.devices.internal.business.config;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceFactory;
import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.HashMap;
import java.util.Map;

@Singleton
public class ConfigurationDeviceBuilder
{
    @Inject
    private DeviceFactory deviceFactory;

    public Map<DeviceRef, Device> getDevices(InstallationConfig config)
    {
        Map<DeviceRef, Device> devices = new HashMap<>();
        for (DeviceConfig deviceConfig : config.getDevices())
        {
            String name = deviceConfig.getName() == null ? deviceConfig.getType().getDefaultName() : deviceConfig.getName();
            if (deviceConfig.getCount() == 1)
            {
                devices.put(new DeviceRef(deviceConfig.getType(), name), deviceFactory.createDevice(deviceConfig));
            }
            else
            {
                for (int i = 0; i < deviceConfig.getCount(); i++)
                {
                    String deviceId = String.format("%s-%02d", name, i + 1);
                    devices.put(new DeviceRef(deviceConfig.getType(), deviceId), deviceFactory.createDevice(deviceConfig));
                }
            }
        }
        return devices;
    }
}