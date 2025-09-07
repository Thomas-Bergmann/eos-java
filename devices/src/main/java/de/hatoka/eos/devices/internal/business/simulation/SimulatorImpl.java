package de.hatoka.eos.devices.internal.business.simulation;

import de.hatoka.eos.devices.capi.business.metrics.ForecastMetricsExporter;
import de.hatoka.eos.devices.capi.business.metrics.SimulationMetricsExporter;
import de.hatoka.eos.devices.capi.business.simulation.SimulationRequest;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.business.simulation.Simulator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class SimulatorImpl implements Simulator
{
    @Inject
    private SimulationMetricsExporter metricsExporter;
    @Inject
    private ForecastMetricsExporter forecastMetricsExporter;

    @Override
    public SimulationResult simulate(SimulationRequest request)
    {
        return new Simulation(request, metricsExporter, forecastMetricsExporter).run();
    }
}
