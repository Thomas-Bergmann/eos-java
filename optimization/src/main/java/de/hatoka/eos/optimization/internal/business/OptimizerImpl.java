package de.hatoka.eos.optimization.internal.business;

import de.hatoka.eos.optimization.capi.business.OptimizationRequest;
import de.hatoka.eos.optimization.capi.business.OptimizationResult;
import de.hatoka.eos.optimization.capi.business.Optimizer;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import de.hatoka.eos.optimization.capi.tasks.CarCharge;
import de.hatoka.eos.simulation.capi.business.config.InstallationConfig;
import de.hatoka.eos.simulation.capi.business.device.Device;
import de.hatoka.eos.simulation.capi.business.device.DeviceFactory;
import de.hatoka.eos.simulation.capi.business.device.DeviceRef;
import de.hatoka.eos.simulation.capi.business.forecast.Forecasts;
import de.hatoka.eos.simulation.capi.business.simulation.EnergySystem;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationRequest;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.simulation.capi.business.simulation.Simulator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.Map;

/**
 * Implementation of an Optimizer
 */
@Singleton
public class OptimizerImpl implements Optimizer
{
    @Inject
    private DeviceFactory deviceFactory;
    @Inject
    private Simulator simulator;

    @Override
    public OptimizationResult optimize(InstallationConfig config, OptimizationGoals goals, OptimizationRequest optimizationRequest)
    {
        int counter = 0;
        CarCharge oneHour = CarCharge.chargeOneHour(optimizationRequest.startDate());
        Map<DeviceRef, Device> devices = deviceFactory.createDevices(config.getDevices());
        devices = oneHour.apply(optimizationRequest.startDate(), devices);
        SimulationRequest request = new SimulationRequest("optimization-" + counter, optimizationRequest.startDate(), optimizationRequest.endDate(), optimizationRequest.stepDuration(),
                        devices, Collections.emptyMap(), Forecasts.STANDARD);
        SimulationResult simResult = simulator.simulate(request);
        return new OptimizationResult(simResult, goals.getPenalty(simResult));
    }
}
