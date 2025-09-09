package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;

public interface Optimizer
{
    OptimizationResult optimize(InstallationConfig config, OptimizationGoals goals);
}
