package de.hatoka.eos.optimization.capi.goals;

import com.fasterxml.jackson.annotation.JsonProperty;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.devices.capi.units.Percentage;

public record PercentagePenalty
(
    @JsonProperty("percentage")
    Percentage percentage,
    @JsonProperty("price")
    Money price
)
{
}
