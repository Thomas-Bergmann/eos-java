package de.hatoka.eos.optimization.internal.business.config;

import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class OptimizationConfigurationLoaderTest
{
    @Test
    public void testLoadOptimizationGoal() throws IOException
    {
        OptimizationConfigurationLoader loader = new OptimizationConfigurationLoader();
        OptimizationGoals goal = loader.load("goal-for-optimization.yaml");
        
        assertNotNull(goal);
        assertNotNull(goal.getCarCharging());
        assertEquals(0.9, goal.getCarCharging().getPercentage().value());
        
        assertNotNull(goal.getCarCharging().getPercentage());
        assertEquals(0.1, goal.getCarCharging().getPenalty().percentage().value());
        
        assertNotNull(goal.getCarCharging().getPenalty().price());
        assertEquals(Money.ofEur(5), goal.getCarCharging().getPenalty().price());
    }
}
