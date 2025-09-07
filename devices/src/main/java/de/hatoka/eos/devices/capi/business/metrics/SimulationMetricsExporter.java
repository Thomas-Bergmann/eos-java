package de.hatoka.eos.devices.capi.business.metrics;

import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;

/**
 * Interface for exporting simulation metrics to time-series databases like InfluxDB.
 */
public interface SimulationMetricsExporter
{
    /**
     * Exports simulation results as time-series metrics.
     *
     * @param result simulation result containing energy flow data
     */
    void exportMetrics(SimulationResult result);
}