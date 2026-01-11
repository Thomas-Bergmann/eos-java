package de.hatoka.eos.persistence.capi.usage;

import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Percentage;

import java.util.Objects;

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
    public static final String COLUMN_CONSUMPTION = "consumption";
    private Energy consumption;
    public static final String COLUMN_PRODUCTION = "production";
    private Energy production;
    public static final String COLUMN_IMPORT = "import";
    private Energy gridImport;
    public static final String COLUMN_EXPORT = "export";
    private Energy gridExport;
    public static final String COLUMN_CHARGE = "charge";
    private Energy charge;
    public static final String COLUMN_DISCHARGE = "discharge";
    private Energy discharge;
    public static final String COLUMN_BATTERY_STATE = "battery";
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

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof UsagePO usagePO)) return false;
        return Objects.equals(consumption, usagePO.consumption) && Objects.equals(production, usagePO.production) && Objects.equals(gridImport,
                        usagePO.gridImport) && Objects.equals(gridExport, usagePO.gridExport) && Objects.equals(charge, usagePO.charge)
                        && Objects.equals(discharge, usagePO.discharge) && Objects.equals(batteryState, usagePO.batteryState);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(consumption, production, gridImport, gridExport, charge, discharge, batteryState);
    }
}
