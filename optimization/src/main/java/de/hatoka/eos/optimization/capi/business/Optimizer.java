package de.hatoka.eos.optimization.capi.business;

import de.hatoka.eos.simulation.capi.business.config.InstallationConfig;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;

/**
 * An Optimizer gets the installation and goals and produces a result of the optimization.
 * The optimizer tries to change the variables (which are not defined yet) to generate a result with a minimum of penalities (see {@link OptimizationResult#getPenalty()}).
 */
public interface Optimizer
{
    OptimizationResult optimize(InstallationConfig config, OptimizationGoals goals);
}
