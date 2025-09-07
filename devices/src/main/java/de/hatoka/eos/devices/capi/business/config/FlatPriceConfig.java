package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.devices.capi.units.Money;

public record FlatPriceConfig(
    @JsonProperty("importPrice")
    @JsonPropertyDescription("Price per kWh when importing energy from the grid (in currency units)")
    Money importPrice,

    @JsonProperty("exportPrice")
    @JsonPropertyDescription("Price per kWh when exporting energy to the grid (in currency units)")
    Money exportPrice
)
{
    /**
     * Configuration with no grid pricing (free import/export).
     */
    public static final FlatPriceConfig NO_PRICING = new FlatPriceConfig(Money.ZERO, Money.ZERO);

    /**
     * Configuration with typical German residential prices.
     */
    public static final FlatPriceConfig GERMAN_RESIDENTIAL = new FlatPriceConfig(Money.ofEur(0.39), Money.ofEur(0.08));


}