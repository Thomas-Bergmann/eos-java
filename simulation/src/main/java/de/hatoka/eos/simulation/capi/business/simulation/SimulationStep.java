package de.hatoka.eos.simulation.capi.business.simulation;

import de.hatoka.eos.simulation.capi.business.forecast.Forecasts;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * SimulationStep are device independent simulation step information
 * @param startDate start of time period to simulate
 * @param duration duration of time period to simulate
 * @param services forecast services
 */
public record SimulationStep(ZonedDateTime startDate, Duration duration, Forecasts services)
{
    public static SimulationStep valueOf(ZonedDateTime startDate, Duration duration)
    {
        return new SimulationStep(startDate, duration, Forecasts.STANDARD);
    }

    public SimulationStep nextTimeSlot()
    {
        return new SimulationStep(startDate.plusMinutes(duration.toMinutes()), duration, services);
    }

    public ZonedDateTime endDate()
    {
        return startDate.plus(duration);
    }
}
