package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceFactory;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.internal.business.statistics.SolarPanelStatisticsLoader;
import jakarta.inject.Singleton;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

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
}