package de.hatoka.eos.optimization.internal.business.config;

import de.hatoka.eos.simulation.internal.business.config.ConfigurationLoader;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

/**
 * OptimizationConfigurationLoader is responsible to load the goals of an optimization
 */
@Singleton
public class OptimizationConfigurationLoader
{
    @Inject
    private ConfigurationLoader loader;

    /**
     * Loads goals of optimization from resource.
     * @param resourcePath path to java-resource
     * @return goals of optimization
     * @throws IOException if goals can't be loaded
     */
    public OptimizationGoals loadGoals(String resourcePath) throws IOException
    {
        return loader.load(resourcePath, OptimizationGoals.class);
    }
}
