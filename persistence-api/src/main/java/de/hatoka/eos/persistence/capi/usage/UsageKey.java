package de.hatoka.eos.persistence.capi.usage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

/**
 * @param time epoch milli seconds
 */
public record UsageKey(long time)
{
    private static final ZoneId UTC = ZoneId.of("UTC");

    public static UsageKey valueOf(Date time)
    {
        return new UsageKey(time.getTime());
    }

    public static UsageKey valueOf(ZonedDateTime time)
    {
        return new UsageKey(time.toInstant().toEpochMilli());
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
