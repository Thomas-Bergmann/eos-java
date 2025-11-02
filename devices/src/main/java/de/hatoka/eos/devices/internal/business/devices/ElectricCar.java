package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.CarUsageProfile;
import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStep;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.units.capi.Energy;

import java.time.LocalTime;
import java.time.ZonedDateTime;

public class ElectricCar extends Battery implements Device
{
    public ElectricCar(DeviceConfig config)
    {
        super(config);
    }

    @Override
    protected boolean shouldDischarge(Energy currentEnergy)
    {
        // Electric car should not discharge to the system, yet
        return false;
    }

    @Override
    public SimulationStepResult simulate(SimulationStep step, EnergySystem system, DeviceState deviceState)
    {
        CarUsageProfile usageProfile = config.getUsageProfile();
        if (usageProfile != null)
        {
            boolean wasAvailable = isCarAvailable(step.startDate().minusMinutes(step.duration().toMinutes()));
            boolean isAvailable = isCarAvailable(step.startDate());

            if (!wasAvailable && isAvailable)
            {
                // Car just returned - apply energy consumption
                deviceState = applyUsageEnergyConsumption(usageProfile, deviceState);
            }
            if (!isAvailable)
            {
                // Car is away - no charging/discharging
                return SimulationStepResult.build(system, deviceState);
            }
        }

        // Car is available - use normal battery simulation
        return super.simulate(step, system, deviceState);
    }

    /**
     * @param time when
     * @return true if the car is available (for charging)
     */
    private boolean isCarAvailable(ZonedDateTime time)
    {
        CarUsageProfile usageProfile = config.getUsageProfile();
        if (usageProfile == null)
        {
            return true; // No usage profile means car is always available
        }

        if (!usageProfile.days().contains(time.getDayOfWeek()))
        {
            return true; // Car is available on days not in the usage profile
        }

        LocalTime currentTime = time.toLocalTime();
        return currentTime.isBefore(usageProfile.startUsage()) || !currentTime.isBefore(usageProfile.endUsage());
    }

    /**
     * Applies the usage consumption to the car at the time, the car is back
     * @param usageProfile usage profile of car
     * @param deviceState old device state
     * @return new device state
     */
    private DeviceState applyUsageEnergyConsumption(CarUsageProfile usageProfile, DeviceState deviceState)
    {
        Energy consumedEnergy = usageProfile.energyConsumption();
        Energy newStoredEnergy = deviceState.storedEnergy().subtract(consumedEnergy);

        // Ensure we don't go below zero
        if (newStoredEnergy.amount() < 0)
        {
            newStoredEnergy = Energy.ZERO;
        }

        return deviceState.apply(newStoredEnergy);
    }
}