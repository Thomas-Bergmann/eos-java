package de.hatoka.eos.persistence.capi.energystock;

import de.hatoka.eos.units.capi.Money;

public class EnergyStockPO
{
    public static final String COLUMN_DAY_AHEAD = "dayaheadprice";
    private Money dayAheadPrice;

    public Money getDayAheadPrice()
    {
        return dayAheadPrice;
    }

    public void setDayAheadPrice(Money dayAheadPrice)
    {
        this.dayAheadPrice = dayAheadPrice;
    }
}
