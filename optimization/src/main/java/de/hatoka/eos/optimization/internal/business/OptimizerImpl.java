package de.hatoka.eos.optimization.internal.business;

import de.hatoka.eos.optimization.capi.business.OptimizationRequest;
import de.hatoka.eos.optimization.capi.business.OptimizationResult;
import de.hatoka.eos.optimization.capi.business.Optimizer;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import de.hatoka.eos.optimization.capi.tasks.CarCharge;
import de.hatoka.eos.simulation.capi.business.config.InstallationConfig;
import de.hatoka.eos.simulation.capi.business.device.DeviceFactory;
import de.hatoka.eos.simulation.capi.business.forecast.Forecasts;
import de.hatoka.eos.simulation.capi.business.simulation.DeviceManipulator;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationRequest;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.simulation.capi.business.simulation.Simulator;
import jakarta.annotation.Nonnull;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Implementation of an Optimizer
 */
@Singleton
public class OptimizerImpl implements Optimizer
{
    private static Logger LOGGER = LoggerFactory.getLogger(OptimizerImpl.class);
    @Inject
    private DeviceFactory deviceFactory;
    @Inject
    private Simulator simulator;

    private final List<List<DeviceManipulator>> toDos = new ArrayList<>();
    private final Set<List<DeviceManipulator>> done = new HashSet<>();

    @Override
    public OptimizationResult optimize(InstallationConfig config, OptimizationGoals goals, OptimizationRequest optimizationRequest)
    {
        // initial without manipulation
        OptimizationResult result = evaluate(config, goals, optimizationRequest, 0, List.of());
        LOGGER.info("initial result {}", result);

        toDos.add(List.of(CarCharge.init(optimizationRequest.startDate(), optimizationRequest.endDate(), 1)));
        for (int counter = 1; counter < 1000 && !toDos.isEmpty(); counter++)
        {
            List<DeviceManipulator> manipulators = toDos.removeFirst();
            OptimizationResult optResult = evaluate(config, goals, optimizationRequest, counter, manipulators);
            if (optResult.isBetterThan(result))
            {
                result = optResult;
                LOGGER.info("found better result {}", optResult);
            }
            else {
                LOGGER.trace("found worse result {}", optResult);
            }
            done.add(manipulators);
            toDos.addAll(evolute(manipulators).stream().filter(this::doesNotExist).toList());
        }
        return result;
    }

    private boolean doesNotExist(List<DeviceManipulator> manipulators)
    {
        return !contains(manipulators);
    }

    private boolean contains(List<DeviceManipulator> manipulators)
    {
        return done.contains(manipulators) || toDos.contains(manipulators);
    }

    private List<List<DeviceManipulator>> evolute(List<DeviceManipulator> manipulators)
    {
        List<List<DeviceManipulator>> result = new ArrayList<>();
        for (DeviceManipulator manipulator : manipulators)
        {
            for (DeviceManipulator child : manipulator.evolute())
            {
                List<DeviceManipulator> others = new ArrayList<>(manipulators);
                others.remove(manipulator);
                others.add(child);
                result.add(others);
            }
        }
        return result;
    }

    @Nonnull
    private OptimizationResult evaluate(InstallationConfig config, OptimizationGoals goals, OptimizationRequest optimizationRequest, int counter,
                    List<DeviceManipulator> manipulators)
    {
        SimulationRequest request = new SimulationRequest("optimization-" + counter, optimizationRequest.startDate(), optimizationRequest.endDate(),
                        optimizationRequest.stepDuration(), deviceFactory.createDevices(config.getDevices()), Collections.emptyMap(),
                        Forecasts.STANDARD);
        SimulationResult simResult = simulator.simulate(request, manipulators);
        return new OptimizationResult(goals.getPenalty(simResult), manipulators);
    }
}
