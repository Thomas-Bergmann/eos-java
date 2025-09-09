package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.devices.capi.units.Percentage;
import de.hatoka.eos.optimization.capi.business.OptimizationGoal;

import java.util.List;
import java.util.Map;

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