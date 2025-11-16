package de.hatoka.eos.persistence.capi.energystock;

import java.time.ZonedDateTime;

public record EnergyStockKey( ZonedDateTime time)
{
    public static EnergyStockKey valueOf(ZonedDateTime time)
    {
        return new EnergyStockKey(time);
    }
}
