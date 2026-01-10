package de.hatoka.eos.optimization.internal.business.config;

import de.hatoka.eos.units.capi.Money;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test loading yaml configuration of optimization goals.
 */
@QuarkusTest
public class OptimizationConfigurationLoaderTest
{
    @Inject
    private OptimizationConfigurationLoader loader;

    @Test
    public void testLoadOptimizationGoal() throws IOException
    {
        OptimizationGoals goal = loader.loadGoals("goal-for-optimization.yaml");
        
        assertNotNull(goal);
        assertNotNull(goal.getCarCharging());
        assertEquals(0.9, goal.getCarCharging().getPercentage().value());
        
        assertNotNull(goal.getCarCharging().getPercentage());
        assertEquals(0.1, goal.getCarCharging().getPenalty().percentage().value());
        
        assertNotNull(goal.getCarCharging().getPenalty().price());
        assertEquals(Money.ofEur(20), goal.getCarCharging().getPenalty().price());
    }
}
