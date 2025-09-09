package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceFactory;
import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.internal.business.statistics.SolarPanelStatisticsLoader;
import jakarta.inject.Singleton;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class DeviceFactoryImpl implements DeviceFactory
{
    @Override
    public Device createDevice(DeviceConfig config)
    {
        try
        {
            DeviceType deviceType = config.getType();
            Class<? extends Device> clazz = deviceType.getDeviceClass();
            Constructor<? extends Device> constructor = clazz.getConstructor(DeviceConfig.class);
            return constructor.newInstance(config);
        }
        catch(InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<DeviceRef, Device> createDevices(List<DeviceConfig> configuredDevices)
    {
        Map<DeviceRef, Device> devices = new HashMap<>();
        for (DeviceConfig deviceConfig : configuredDevices)
        {
            String name = deviceConfig.getName() == null ? deviceConfig.getType().getDefaultName() : deviceConfig.getName();
            if (deviceConfig.getCount() == 1)
            {
                devices.put(new DeviceRef(deviceConfig.getType(), name), createDevice(deviceConfig));
            }
            else
            {
                for (int i = 0; i < deviceConfig.getCount(); i++)
                {
                    String deviceId = String.format("%s-%02d", name, i + 1);
                    devices.put(new DeviceRef(deviceConfig.getType(), deviceId), createDevice(deviceConfig));
                }
            }
        }
        return devices;
    }
}