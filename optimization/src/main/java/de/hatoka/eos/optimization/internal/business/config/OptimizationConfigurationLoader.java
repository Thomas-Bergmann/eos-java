package de.hatoka.eos.optimization.internal.business.config;

import de.hatoka.eos.devices.internal.business.config.ConfigurationLoader;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;

@Singleton
public class OptimizationConfigurationLoader
{
    @Inject
    private ConfigurationLoader loader;

    public OptimizationGoals loadGoals(String resourcePath) throws IOException
    {
        return loader.load(resourcePath, OptimizationGoals.class);
    }
}
