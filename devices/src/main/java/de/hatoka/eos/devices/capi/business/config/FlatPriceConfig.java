package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.devices.capi.units.Money;

/**
 * The FlatPriceConfig defines the import and export prices, which are static or flat.
 * @param importPrice import price per kWh
 * @param exportPrice export price/yield per kWh
 */
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
     * Configuration with typical German residential prices.
     */
    public static final FlatPriceConfig GERMAN_RESIDENTIAL = new FlatPriceConfig(Money.ofEur(0.39), Money.ofEur(0.08));
}