package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.simulation.capi.business.device.DeviceState;
import de.hatoka.eos.simulation.capi.business.device.DeviceType;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.units.capi.Money;
import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.optimization.capi.business.OptimizationGoal;

import java.util.List;
import java.util.Map;

/**
 * A CarChargeGoal represents the goal to have a fully charged cars. The penalty is applied fluently, means that also "sub blocks" of the percentage
 * penalty are applied.
 * <li>The "percentage" is the charge of the battery to reach the goal. E.g. 80%</li>
 * <li>The "penalty" contains the penalty for each missed percent. E.g. 1EUR for every missed percent</li>
 */
public class CarChargeGoal implements OptimizationGoal
{
    @JsonProperty("percentage")
    private Percentage percentage;

    @JsonProperty("penalty")
    private PercentagePenalty penalty;

    /**
     * @return penalty if goal is not reached
     */
    public PercentagePenalty getPenalty()
    {
        return penalty;
    }

    /**
     * @return percentage to reach
     */
    public Percentage getPercentage()
    {
        return percentage;
    }

    /**
     * @param simulationResult result of simulation
     * @return accumulated penalty for all electric cars in the simulation
     */
    @Override
    public Money getPenalty(SimulationResult simulationResult)
    {
        List<Percentage> reached = simulationResult.endState()
                                                   .entrySet()
                                                   .stream()
                                                   .filter(e -> e.getKey().type().equals(DeviceType.ELECTRIC_CAR))
                                                   .map(Map.Entry::getValue)
                                                   .map(DeviceState::percentage)
                        .filter(p -> p.lessThan(percentage))
                                                   .toList();

        Money totalPenalty = Money.ZERO;
        for (Percentage reachedPercentage : reached)
        {
            // Calculate the shortfall in percentage points
            double shortfall = percentage.value() - reachedPercentage.value();
            // Calculate how many penalty blocks the shortfall represents using the penalty percentage
            double penaltyBlocks = shortfall / penalty.percentage().value();
            // Apply penalty for each penalty percentage block of shortfall
            totalPenalty = totalPenalty.add(penalty.price().multiply(penaltyBlocks));
        }
        return totalPenalty;
    }
}