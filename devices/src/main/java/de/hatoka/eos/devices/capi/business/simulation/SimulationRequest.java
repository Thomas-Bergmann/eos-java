package de.hatoka.eos.devices.capi.business.simulation;

import de.hatoka.eos.devices.capi.business.config.ChargingConfig;
import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.forecast.Forecasts;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Map;

public record SimulationRequest(String simulationId, ZonedDateTime startDate, ZonedDateTime endDate, Duration stepDuration, Map<DeviceRef, Device> devices,
                Map<DeviceRef, DeviceState> initialState, ChargingConfig chargingConfig, Forecasts services
)
{
    public SimulationStep getFirstStep()
    {
        return new SimulationStep(startDate(), stepDuration(), chargingConfig(), services());
    }

}
