package de.hatoka.eos.units.capi;

public enum EnergyUnits
{
    K_WH("kWh");

    private final String symbol;
    EnergyUnits(String symbol)
    {
        this.symbol = symbol;
    }
    public String getSymbol()
    {
        return symbol;
    }
}
