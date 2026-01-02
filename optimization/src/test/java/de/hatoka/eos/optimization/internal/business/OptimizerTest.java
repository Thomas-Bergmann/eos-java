package de.hatoka.eos.optimization.internal.business;

import de.hatoka.eos.optimization.capi.business.OptimizationRequest;
import de.hatoka.eos.simulation.capi.business.config.InstallationConfig;
import de.hatoka.eos.simulation.capi.business.config.SimulationConfig;
import de.hatoka.eos.units.capi.Money;
import de.hatoka.eos.simulation.internal.business.config.ConfigurationLoader;
import de.hatoka.eos.optimization.capi.business.OptimizationResult;
import de.hatoka.eos.optimization.capi.business.Optimizer;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import de.hatoka.eos.optimization.internal.business.config.OptimizationConfigurationLoader;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that Optimizer find a proper "execution plan".
 */
@QuarkusTest
public class OptimizerTest
{
    @Inject
    private ConfigurationLoader configurationLoader;
    @Inject
    private OptimizationConfigurationLoader optimizationConfigurationLoader;
    @Inject
    private Optimizer optimizer;

    /**
     * Test starting point, no simulation at all.
     */
    @Test
    public void testDoNothing() throws IOException
    {
        InstallationConfig config = configurationLoader.loadInstallation("test-installation-for-optimization-car-only.yaml");
        SimulationConfig simulationConfig = configurationLoader.loadSimulation("test-simulation.yaml");
        OptimizationGoals goals = optimizationConfigurationLoader.loadGoals("goal-for-optimization.yaml");
        OptimizationRequest request = OptimizationRequest.valueOf(simulationConfig);
        OptimizationResult result = optimizer.optimize(config, goals, request);
        // starts at 80% goal is 90% makes 10% difference, 5 EUR
        assertEquals(Money.ofEur(5), result.getPenalty());
    }
}
