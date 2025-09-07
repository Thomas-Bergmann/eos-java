package de.hatoka.eos.devices.capi.business.simulation;

import de.hatoka.eos.devices.capi.business.config.ChargingConfig;
import de.hatoka.eos.devices.capi.business.forecast.Forecasts;

import java.time.Duration;
import java.time.ZonedDateTime;

/**
 * SimulationStep are device independent simulation step information
 * @param startDate start of time period to simulate
 * @param duration duration of time period to simulate
 * @param chargingConfig configuration how charging devices should react
 * @param services forecast services
 */
public record SimulationStep(ZonedDateTime startDate, Duration duration, ChargingConfig chargingConfig, Forecasts services)
{
    public static SimulationStep valueOf(ZonedDateTime startDate, Duration duration)
    {
        return valueOf(startDate, duration, ChargingConfig.ONLY_PRODUCED_ENERGY);
    }

    public static SimulationStep valueOf(ZonedDateTime startDate, Duration duration, ChargingConfig chargingConfig)
    {
        return new SimulationStep(startDate, duration, chargingConfig, Forecasts.STANDARD);
    }

    public SimulationStep nextTimeSlot()
    {
        return new SimulationStep(startDate.plusMinutes(duration.toMinutes()), duration, chargingConfig, services);
    }

    public ZonedDateTime endDate()
    {
        return startDate.plus(duration);
    }
}
