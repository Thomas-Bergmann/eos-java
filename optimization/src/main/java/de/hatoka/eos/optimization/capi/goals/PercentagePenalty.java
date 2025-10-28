package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.devices.capi.units.Percentage;

/**
 * PercentagePenalty declares the penalty for non reached percentage goals.
 * @param percentage step or block, how often the penalty must be applied
 * @param price money value of one penalty block
 */
public record PercentagePenalty
(
    @JsonProperty("percentage")
    Percentage percentage,
    @JsonProperty("price")
    Money price
)
{
}
