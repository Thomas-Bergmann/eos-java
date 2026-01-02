package de.hatoka.eos.optimization.capi.tasks;

import de.hatoka.eos.simulation.capi.business.device.Device;
import de.hatoka.eos.simulation.capi.business.device.DeviceRef;
import de.hatoka.eos.simulation.capi.business.device.DeviceType;
import de.hatoka.eos.simulation.internal.business.devices.ElectricCar;
import de.hatoka.eos.units.capi.Percentage;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public record CarCharge(Percentage chargeLimit, ZonedDateTime from, ZonedDateTime to)
{
    public static CarCharge chargeOneHour(ZonedDateTime startTime)
    {
        return new CarCharge(Percentage.ONE_HUNDRED, startTime, startTime.plusHours(1));
    }

    public Map<DeviceRef, Device> apply(ZonedDateTime time, Map<DeviceRef, Device> devices)
    {
        Map<DeviceRef, Device> result = new HashMap<>();
        devices.forEach((r, d) -> {
            result.put(r, apply(time, r, d));
        });
        return result;
    }

    private Device apply(ZonedDateTime time, DeviceRef deviceRef, Device device)
    {
        if (!DeviceType.ELECTRIC_CAR.equals(deviceRef.type()))
        {
            return device;
        }
        if (device instanceof ElectricCar car)
        {
            if (time.isBefore(from) || time.isAfter(to))
            {
                return car.resetOverrideForceChargingLimit();
            }
            return car.setOverrideForceChargingLimit(chargeLimit);
        }
        return device;
    }
}
