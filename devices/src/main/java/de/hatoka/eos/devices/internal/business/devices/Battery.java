package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStep;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Percentage;
import de.hatoka.eos.devices.capi.units.Power;

import java.time.ZonedDateTime;

public class Battery implements Device
{
    final DeviceConfig config;

    public Battery(DeviceConfig config)
    {
        this.config = config;
    }

    @Override
    public DeviceState getInitialState()
    {
        return new DeviceState(config.getCapacity(), config.getStartStorageLevel());
    }

    @Override
    public SimulationStepResult simulate(SimulationStep step, EnergySystem system, DeviceState deviceState)
    {
        // Apply storage loss first
        deviceState = applyStorageLoss(step, deviceState);

        Energy systemEnergy = system.getCurrentEnergy();

        if (shouldCharge(getChargingLimit(step), deviceState, systemEnergy))
        {
            return chargeBattery(step, system, deviceState);
        }
        if (shouldDischarge(systemEnergy))
        {
            // System needs energy - try to discharge battery
            return dischargeBattery(step, system, deviceState);
        }
        return SimulationStepResult.build(system, deviceState);
    }

    private DeviceState applyStorageLoss(SimulationStep step, DeviceState deviceState)
    {
        Percentage perc = config.getDailyStorageLoss().multiply((double)step.duration().toMinutes() / 60 / 24);
        Energy lossEnergy = deviceState.storedEnergy().multiply(perc);
        return deviceState.apply(deviceState.storedEnergy().subtract(lossEnergy));
    }

    private SimulationStepResult chargeBattery(SimulationStep step, EnergySystem system, DeviceState deviceState)
    {
        Energy systemEnergy = system.getCurrentEnergy();
        Energy currentStored = deviceState.storedEnergy();
        // Maximum energy we can charge based on power limit and duration
        Energy maxCharge = config.getChargeRate().multiply(step.duration());

        // Maximum energy we can store (capacity - current)
        Energy maxStore = config.getCapacity().subtract(currentStored);

        Energy actualCharge = systemEnergy.min(maxCharge).min(maxStore);
        if (actualCharge.amount() > 0)
        {
            // Apply charging efficiency
            Energy storedEnergy = actualCharge.multiply(config.getChargingEfficiency());
            system = system.charge(actualCharge);
            deviceState = deviceState.apply(currentStored.add(storedEnergy));
        }
        else
        {
            actualCharge = Energy.ZERO;
        }
        if (shouldChargeFromGrid(step.startDate(), getChargingLimit(step), deviceState))
        {
            // Battery is not charged above the limit, so charge it from grid
            return chargeBatteryFromGrid(step, system, deviceState, actualCharge);
        }
        return SimulationStepResult.build(system, deviceState);
    }

    private SimulationStepResult chargeBatteryFromGrid(SimulationStep step, EnergySystem system, DeviceState deviceState, Energy actualCharge)
    {
        Energy currentStored = deviceState.storedEnergy();

        // Maximum energy we can charge based on power limit and duration
        Energy maxCharge = config.getChargeRate().multiply(step.duration()).subtract(actualCharge);

        // Maximum energy we can store (capacity - current)
        Energy maxStore = config.getCapacity().subtract(currentStored);

        Percentage percentageToLoadFromGrid = getChargingLimit(step).subtract(deviceState.percentage());
        Energy storeToGridThreshold = deviceState.maxEnergy().multiply(percentageToLoadFromGrid);

        Energy actualChargeFromGrid = maxCharge.min(maxStore).min(storeToGridThreshold);

        // Apply charging efficiency
        Energy storedEnergyFromGrid = actualChargeFromGrid.multiply(config.getChargingEfficiency());
        Energy newStoredEnergy = currentStored.add(storedEnergyFromGrid);

        // Battery consumes the charged energy from the system
        return SimulationStepResult.build(system.charge(actualChargeFromGrid), deviceState.apply(newStoredEnergy));
    }

    private SimulationStepResult dischargeBattery(SimulationStep step, EnergySystem system, DeviceState deviceState)
    {
        // Maximum energy we can discharge based on power limit and duration
        Energy maxDischarge = config.getDischargeRate().multiply(step.duration());

        // Energy needed by the system (positive value)
        Energy required = system.getCurrentEnergy().negate();

        // Actual discharging energy is limited by available stored energy, discharge power, and system need
        Energy currentStored = deviceState.storedEnergy();

        Energy actualDischargeEnergy = required.min(currentStored).min(maxDischarge);

        // Apply discharging efficiency
        Energy providedEnergy = actualDischargeEnergy.multiply(config.getDischargingEfficiency());
        Energy newStoredEnergy = currentStored.subtract(actualDischargeEnergy);

        // Battery provides energy to the system
        return SimulationStepResult.build(system.discharge(providedEnergy), deviceState.apply(newStoredEnergy));
    }


    protected boolean shouldChargeFromGrid(ZonedDateTime time, Percentage chargingLimit, DeviceState deviceState)
    {
        // If the charging limit is higher than the current battery percentage, we can charge from grid
        return chargingLimit.value() > deviceState.percentage().value();
    }

    protected boolean shouldCharge(Percentage chargingForceLimit, DeviceState deviceState, Energy systemEnergy)
    {
        return systemEnergy.amount() > 0 || chargingForceLimit.value() > deviceState.percentage().value();
    }

    protected boolean shouldDischarge(Energy systemEnergy)
    {
        // Discharge if there is a need for energy in the system
        return systemEnergy.amount() < 0;
    }

    /**
     * Returns the charging limit for this device type.
     * Battery uses forceChargingLimit from DeviceConfig with default of 0% (no force charging).
     * @param step time of charge (may it's different percentage)
     */
    protected Percentage getChargingLimit(SimulationStep step)
    {
        return config.getForceChargingLimit();
    }
}
