package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.devices.capi.business.simulation.*;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Money;

public class Grid implements Device
{
    public Grid()
    {
    }

    /**
     * Required constructor
     */
    public Grid(DeviceConfig config)
    {
    }

    @Override
    public SimulationStepResult simulate(SimulationStep step, EnergySystem system, DeviceState deviceState)
    {
        Energy currentEnergy = system.getCurrentEnergy();
        boolean powerGenerated = currentEnergy.amount() > 0;
        Energy toSystem = powerGenerated ? Energy.ZERO : currentEnergy.negate();
        Energy fromSystem = powerGenerated ? currentEnergy : Energy.ZERO;

        // Create grid energy transfer with costs
        EnergyPriceForecast energyPriceProvider = step.services().priceForecast();
        if (powerGenerated)
        {
            // System has excess energy - grid exports it (we receive money from grid)
            Money receivedFromGrid = energyPriceProvider.getExportPrice(step.startDate()).multiply(fromSystem.amount());
            system = system.exportEnergy(fromSystem, receivedFromGrid);
        }
        else if (currentEnergy.amount() < 0)
        {
            // System needs energy - grid imports it (we pay money to grid)
            Money paidToGrid = energyPriceProvider.getImportPrice(step.startDate()).multiply(toSystem.amount());
            system = system.importEnergy(toSystem, paidToGrid);
        }
        return SimulationStepResult.build(system);
    }
}
