package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.simulation.capi.business.config.SimulationConfig;
import de.hatoka.eos.simulation.capi.business.config.SimulationTimeSettings;

import java.time.Duration;
import java.time.ZonedDateTime;

public record OptimizationRequest(ZonedDateTime startDate, ZonedDateTime endDate, Duration stepDuration)
{
    public static OptimizationRequest valueOf(SimulationConfig simulationConfig)
    {
        SimulationTimeSettings timeSettings = simulationConfig.getTimeSettings();
        return new OptimizationRequest(timeSettings.getZonedStartTime(), timeSettings.getZonedEndTime(), timeSettings.getStepDuration());
    }
}
