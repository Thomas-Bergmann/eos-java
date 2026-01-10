package de.hatoka.eos.persistence.capi.usage;

import de.hatoka.eos.units.capi.Energy;

public class UsagePO
{
    private Energy consumption;
    private Energy production;

    public Energy getConsumption()
    {
        return consumption;
    }

    public void setConsumption(Energy consumption)
    {
        this.consumption = consumption;
    }

    public Energy getProduction()
    {
        return production;
    }

    public void setProduction(Energy production)
    {
        this.production = production;
    }
}
