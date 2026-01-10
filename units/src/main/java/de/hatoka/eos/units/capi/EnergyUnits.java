package de.hatoka.eos.units.capi;

public enum EnergyUnits
{
    K_WH("kWh", 1000),
    WH("Wh", 1);

    private final String symbol;
    private final int baseUnit;

    EnergyUnits(String symbol, int baseUnit)
    {
        this.symbol = symbol;
        this.baseUnit = baseUnit;
    }

    public String getSymbol()
    {
        return symbol;
    }

    public int getBaseUnit()
    {
        return baseUnit;
    }
}
