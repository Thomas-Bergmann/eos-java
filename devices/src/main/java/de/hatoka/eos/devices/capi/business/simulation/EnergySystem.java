package de.hatoka.eos.devices.capi.business.simulation;

import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Money;

/**
 * EnergySystem represents the energy flow in a system, including production, charging, discharging, consumption, import, export, and costs. All
 * numbers are positive.
 *
 * @param produced produced energy in the system, like a solar panel
 * @param charged charged energy in the system, like a battery being charged
 * @param discharged discharged energy in the system, like a battery being discharged
 * @param consumed consumed energy in the system, like a device using power
 * @param imported imported energy into the system, like a grid connection
 * @param exported exported energy from the system, like a grid connection
 * @param importRevenue money transfer for using energy (mostly negative, because of buying energy)
 * @param exportRevenue money transfer for exporting energy (mostly positive, because of "giving" energy, exception negative energy prices)
 */
public record EnergySystem(Energy produced, Energy charged, Energy discharged, Energy consumed, Energy imported, Energy exported, Money importRevenue,
                Money exportRevenue)
{
    public static final EnergySystem INIT = new EnergySystem(Energy.ZERO, Energy.ZERO, Energy.ZERO, Energy.ZERO, Energy.ZERO, Energy.ZERO, Money.ZERO,
                    Money.ZERO);

    /**
     * Produces energy in the system, like a solar panel.
     *
     * @param energy add energy to the system
     * @return System after producing
     */
    public EnergySystem produce(Energy energy)
    {
        return new EnergySystem(produced.add(energy), charged, discharged, consumed, imported, exported, importRevenue, exportRevenue);
    }

    /**
     * Charges the system with energy, like a battery being discharged.
     *
     * @param energy add energy to the system
     * @return System after charging
     */
    public EnergySystem discharge(Energy energy)
    {
        return new EnergySystem(produced, charged, discharged.add(energy), consumed, imported, exported, importRevenue, exportRevenue);
    }

    /**
     * Energy is used to charge a device, like a battery being charged.
     *
     * @param energy removes energy from the system
     * @return System after consuming
     */
    public EnergySystem charge(Energy energy)
    {
        return new EnergySystem(produced, charged.add(energy), discharged, consumed, imported, exported, importRevenue, exportRevenue);
    }

    /**
     * Consumes energy in the system, like a device using power.
     *
     * @param energy remove energy from the system
     * @return System after consuming
     */
    public EnergySystem consume(Energy energy)
    {
        return new EnergySystem(produced, charged, discharged, consumed.add(energy), imported, exported, importRevenue, exportRevenue);
    }

    /**
     * Imports energy into the system, like a grid connection.
     *
     * @param energy add energy to the system
     * @param costsFromGrid costs for importing the energy (positive value)
     * @return System after importing
     */
    public EnergySystem importEnergy(Energy energy, Money costsFromGrid)
    {
        return new EnergySystem(produced, charged, discharged, consumed, imported.add(energy), exported, this.importRevenue.subtract(costsFromGrid),
                        exportRevenue);
    }

    /**
     * Exports energy from the system, like a grid connection.
     *
     * @param energy remove energy from the system
     * @param receivedFromGrid money received from the grid for exported energy (positive value)
     * @return System after exporting
     */
    public EnergySystem exportEnergy(Energy energy, Money receivedFromGrid)
    {
        return new EnergySystem(produced, charged, discharged, consumed, imported, exported.add(energy), importRevenue,
                        this.exportRevenue.add(receivedFromGrid));
    }

    /**
     * @return current energy in the system
     */
    public Energy getCurrentEnergy()
    {
        return produced.subtract(charged).add(discharged).subtract(consumed).add(imported).subtract(exported);
    }

    /**
     * @return current revenue for energy transfer from the grid (negative if costs)
     */
    public Money getEnergyRevenue()
    {
        return importRevenue.add(exportRevenue);
    }

    /**
     * @param subtrahend previous state of energy system
     * @return difference to current energy system
     */
    public EnergySystem subtract(EnergySystem subtrahend)
    {
        return new EnergySystem(produced.subtract(subtrahend.produced), charged.subtract(subtrahend.charged),
                        discharged.subtract(subtrahend.discharged), consumed.subtract(subtrahend.consumed), imported.subtract(subtrahend.imported),
                        exported.subtract(subtrahend.exported), importRevenue.subtract(subtrahend.importRevenue), exportRevenue.subtract(subtrahend.exportRevenue));
    }
}
