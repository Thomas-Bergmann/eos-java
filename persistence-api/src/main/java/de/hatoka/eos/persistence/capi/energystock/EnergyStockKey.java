package de.hatoka.eos.persistence.capi.energystock;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @param time epoch milli seconds
 */
public record EnergyStockKey(long time)
{
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static EnergyStockKey valueOf(ZonedDateTime time)
    {
        return new EnergyStockKey(time.toInstant().toEpochMilli());
    }

    public Instant getInstant()
    {
        return Instant.ofEpochMilli(time);
    }

    public ZonedDateTime getZonedDateTime()
    {
        return getInstant().atZone(UTC);
    }

}
