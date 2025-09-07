package de.hatoka.eos.devices.internal.business.simulation;

import de.hatoka.eos.devices.capi.business.device.Device;
import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.metrics.ForecastMetricsExporter;
import de.hatoka.eos.devices.capi.business.metrics.SimulationMetricsExporter;
import de.hatoka.eos.devices.capi.business.simulation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Simulation
{
    private static final Logger LOGGER = LoggerFactory.getLogger(Simulation.class);
    private final SimulationRequest request;
    private final SimulationMetricsExporter simulationMetricsExporter;
    private final ForecastMetricsExporter forecastMetricsExporter;

    private Map<DeviceRef, DeviceState> currentState;

    public Simulation(SimulationRequest request, SimulationMetricsExporter simulationMetricsExporter, ForecastMetricsExporter forecastMetricsExporter)
    {
        this.request = request;
        this.simulationMetricsExporter = simulationMetricsExporter;
        this.forecastMetricsExporter = forecastMetricsExporter;
    }

    public SimulationResult run()
    {
        this.currentState = new HashMap<>(this.request.initialState());
        SimulationStep currentStep = request.getFirstStep();

        EnergySystem system = EnergySystem.INIT;
        while(currentStep.startDate().isBefore(this.request.endDate()))
        {
            system = executeStep(currentStep, system);
            currentStep = currentStep.nextTimeSlot();
        }
        return new SimulationResult(request, currentStep, currentState, system);
    }

    private String toString(ZonedDateTime time)
    {
        return String.format("%02dT%02d:%02d",time.getDayOfMonth(), time.getHour(), time.getMinute());
    }

    private EnergySystem executeStep(SimulationStep step, EnergySystem system)
    {
        List<DeviceRef> orderedDevices = orderDevices();
        EnergySystem updatedSystem = system;
        for (DeviceRef deviceRef : orderedDevices)
        {
            Device device = request.devices().get(deviceRef);
            DeviceState deviceState = currentState.computeIfAbsent(deviceRef, (d) -> device.getInitialState());
            SimulationStepResult stepResult = device.simulate(step, updatedSystem, deviceState);
            if (!stepResult.deviceState().equals(deviceState))
            {
                LOGGER.trace("device changed {}@{} {}", deviceRef, toString(step.startDate()), stepResult.deviceState());
            }
            if (!updatedSystem.equals(stepResult.system()))
            {
                LOGGER.trace("system changed {}@{} {}", deviceRef, toString(step.startDate()), stepResult.system());
            }
            updatedSystem = stepResult.system();
            currentState.put(deviceRef, stepResult.deviceState());
        }
        simulationMetricsExporter.exportMetrics(new SimulationResult(request, step, currentState, updatedSystem.subtract(system)));
        forecastMetricsExporter.export(step.startDate(), request.services().priceForecast());
        return updatedSystem;
    }

    /**
     * Creates the priorities of each device
     *
     * @return list of devices ordered by priority
     */
    private List<DeviceRef> orderDevices()
    {
        return request.devices().keySet().stream().sorted(Comparator.comparingInt(a -> a.type().ordinal())).toList();
    }
}
