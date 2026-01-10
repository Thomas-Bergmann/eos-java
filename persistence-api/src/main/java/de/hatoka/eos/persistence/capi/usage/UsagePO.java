package de.hatoka.eos.persistence.capi.usage;

import de.hatoka.eos.units.capi.Energy;

public class UsagePO
{
    private Energy consumption;
    private Energy production;
    private Energy gridImport;
    private Energy gridExport;

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

    public Energy getGridImport()
    {
        return gridImport;
    }

    public void setGridImport(Energy gridImport)
    {
        this.gridImport = gridImport;
    }

    public Energy getGridExport()
    {
        return gridExport;
    }

    public void setGridExport(Energy gridExport)
    {
        this.gridExport = gridExport;
    }
}
