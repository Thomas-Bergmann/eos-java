package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStep;
import de.hatoka.eos.devices.capi.business.statistics.SolarPanelStatistics;
import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.units.capi.Power;

public class SolarPanel implements Device
{
    private final DeviceConfig config;
    private final SolarPanelStatistics statistics;

    public SolarPanel(DeviceConfig config)
    {
        this.config = config;
        this.statistics = config.getSolarPanelStatistics();
    }

    @Override
    public SimulationStepResult simulate(SimulationStep step, EnergySystem system, DeviceState deviceState)
    {
        Percentage sunFactor = step.services().weather().getSunProbability(step.startDate());
        Percentage statisticsEfficiency = statistics.getEfficiency(step.startDate());
        Percentage panelEfficiency = getPanelEfficiency();
        
        Power adjustedProduction = getProduction()
            .multiply(sunFactor)
            .multiply(statisticsEfficiency)
            .multiply(panelEfficiency);
            
        return SimulationStepResult.build(system.produce(adjustedProduction.multiply(step.duration())));
    }

    private Power getProduction()
    {
        return config.getProduction();
    }

    private Percentage getPanelEfficiency()
    {
        return config.getPanelEfficiency();
    }
}
