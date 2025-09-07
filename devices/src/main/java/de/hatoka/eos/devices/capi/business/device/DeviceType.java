package de.hatoka.eos.devices.capi.business.device;

import de.hatoka.eos.devices.internal.business.devices.Battery;
import de.hatoka.eos.devices.internal.business.devices.ElectricCar;
import de.hatoka.eos.devices.internal.business.devices.Grid;
import de.hatoka.eos.devices.internal.business.devices.NoisyUsage;
import de.hatoka.eos.devices.internal.business.devices.SolarPanel;

public enum DeviceType
{
    SOLAR_PANEL(SolarPanel.class, "Panel"), NOISY_USAGE(NoisyUsage.class, "NoisyUsage"), BATTERY(Battery.class, "Battery"), ELECTRIC_CAR(ElectricCar.class, "ElectricCar"), GRID(Grid.class, "Grid");

    private final Class<? extends Device> deviceClass;
    private final String defaultDeviceName;
    DeviceType(Class<? extends Device> deviceClass, String defaultDeviceName)
    {
        this.deviceClass = deviceClass;
        this.defaultDeviceName = defaultDeviceName;
    }

    public Class<? extends Device> getDeviceClass()
    {
        return deviceClass;
    }

    public String getDefaultName()
    {
        return defaultDeviceName;
    }
}
