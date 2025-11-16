package de.hatoka.eos.simulation.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

/**
 * The GridConfig is the container for grid pricing configuration. The type can be CSV or FLAT and selects the related configuration.
 * @param type CSV or FLAT
 * @param flatPriceConfig flat price configuration
 */
public record GridConfig (
    @JsonProperty("type")
    @JsonPropertyDescription("select csvPriceProvider or flatPriceProvider")
    GridPriceConfigType type,

    @JsonProperty("dynamicPriceConfig")
    @JsonPropertyDescription("price provider based on DAO with configuration")
    DynamicPriceConfig dynamicPriceConfig,

    @JsonProperty("flatPriceProvider")
    @JsonPropertyDescription("Flat price provider configuration")
    FlatPriceConfig flatPriceConfig
)
{
}