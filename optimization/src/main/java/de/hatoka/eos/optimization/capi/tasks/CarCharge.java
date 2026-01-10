package de.hatoka.eos.optimization.capi.tasks;

import de.hatoka.eos.simulation.capi.business.device.Device;
import de.hatoka.eos.simulation.capi.business.device.DeviceRef;
import de.hatoka.eos.simulation.capi.business.device.DeviceType;
import de.hatoka.eos.simulation.capi.business.simulation.DeviceManipulator;
import de.hatoka.eos.simulation.internal.business.devices.ElectricCar;
import de.hatoka.eos.units.capi.Percentage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record CarCharge(int hours, long from, long to, long start) implements DeviceManipulator
{
    public static CarCharge valueOf(ZonedDateTime from, ZonedDateTime to, ZonedDateTime start, int hours)
    {
        return new CarCharge(hours, from.toInstant().toEpochMilli(), to.toInstant().toEpochMilli(), start.toInstant().toEpochMilli());
    }

    public static CarCharge init(ZonedDateTime from, ZonedDateTime to, int hours)
    {
        return valueOf(from, to, from, hours);
    }

    @Override
    public Map<DeviceRef, Device> apply(ZonedDateTime time, Map<DeviceRef, Device> devices)
    {
        Map<DeviceRef, Device> result = new HashMap<>();
        devices.forEach((r, d) -> {
            result.put(r, apply(time, r, d));
        });
        return result;
    }
    private static final ZoneId UTC = ZoneId.of("UTC");

    public ZonedDateTime getZonedDateTime(long time)
    {
        return Instant.ofEpochMilli(time).atZone(UTC);
    }

    private Device apply(ZonedDateTime time, DeviceRef deviceRef, Device device)
    {
        if (!DeviceType.ELECTRIC_CAR.equals(deviceRef.type()))
        {
            return device;
        }
        if (device instanceof ElectricCar car)
        {
            if (time.isBefore(getZonedDateTime(start)) || time.isAfter(getZonedDateTime(start).plusHours(hours)))
            {
                return car.resetOverrideForceChargingLimit();
            }
            return car.setOverrideForceChargingLimit(Percentage.ONE_HUNDRED);
        }
        return device;
    }

    @Override
    public List<DeviceManipulator> evolute()
    {
        List<DeviceManipulator> result = new ArrayList<>();
        if (getZonedDateTime(start()).plusHours(1).isBefore(getZonedDateTime(to)))
        {
            result.add(new CarCharge(hours, from, to, getZonedDateTime(start).plusHours(1).toInstant().toEpochMilli()));
        }
        if (!getZonedDateTime(from).isAfter(getZonedDateTime(start()).minusHours(1)))
        {
            result.add(new CarCharge(hours, from, to, getZonedDateTime(start).minusHours(1).toInstant().toEpochMilli()));
        }
        result.add(new CarCharge(hours + 1, from, to, start));
        if (hours - 1 > 0)
        {
            result.add(new CarCharge(hours - 1, from, to, start));
        }
        return result;
    }
}
