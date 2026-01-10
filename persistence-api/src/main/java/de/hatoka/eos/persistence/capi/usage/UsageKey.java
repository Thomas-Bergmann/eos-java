package de.hatoka.eos.persistence.capi.usage;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
 * @param time epoch milli seconds
 */
public record UsageKey(long time)
{
    private static final ZoneId UTC = ZoneId.of("UTC");

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
