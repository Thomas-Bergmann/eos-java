package de.hatoka.eos.persistence.capi.usage;

import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Percentage;

/**
 * Usage persistence, energy sign is defined via increment or decrement to the system, means
 * <li>consumption - negative</li>
 * <li>production - positive</li>
 * <li>gridExport - negative</li>
 * <li>gridImport - positive</li>
 * <li>charge - negative</li>
 * <li>discharge - positive</li>
 */
public class UsagePO
{
    private Energy consumption;
    private Energy production;
    private Energy gridImport;
    private Energy gridExport;
    private Energy charge;
    private Energy discharge;
    private Percentage batteryState;

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

    public Energy getCharge()
    {
        return charge;
    }

    public void setCharge(Energy charge)
    {
        this.charge = charge;
    }

    public Energy getDischarge()
    {
        return discharge;
    }

    public void setDischarge(Energy discharge)
    {
        this.discharge = discharge;
    }

    public Percentage getBatteryState()
    {
        return batteryState;
    }

    public void setBatteryState(Percentage batteryState)
    {
        this.batteryState = batteryState;
    }
}
