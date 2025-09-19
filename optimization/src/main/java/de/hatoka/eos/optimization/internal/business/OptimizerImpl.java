package de.hatoka.eos.optimization.internal.business;

import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.business.device.DeviceFactory;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.optimization.capi.business.OptimizationResult;
import de.hatoka.eos.optimization.capi.business.Optimizer;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

/**
 * Implementation of an Optimizer
 */
@Singleton
public class OptimizerImpl implements Optimizer
{
    @Inject
    private DeviceFactory deviceFactory;

    @Override
    public OptimizationResult optimize(InstallationConfig config, OptimizationGoals goals)
    {
        SimulationResult simResult = new SimulationResult(null, null, deviceFactory.createInitialState(config.getDevices()), EnergySystem.INIT);
        return new OptimizationResult(simResult, goals.getPenalty(simResult));
    }
}
