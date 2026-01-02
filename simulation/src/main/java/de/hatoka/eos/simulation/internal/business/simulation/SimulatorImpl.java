package de.hatoka.eos.simulation.internal.business.simulation;

import de.hatoka.eos.simulation.capi.business.metrics.SimulationMetricsExporter;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationRequest;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.simulation.capi.business.simulation.Simulator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SimulatorImpl implements Simulator
{
    @Inject
    private SimulationMetricsExporter metricsExporter;

    @Override
    public SimulationResult simulate(SimulationRequest request)
    {
        return new Simulation(request, metricsExporter).run();
    }
}
