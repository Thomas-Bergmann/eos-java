package de.hatoka.eos.simulation.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Money;

/**
 * The ImportExportChargeConfig defines additional charge or fee for import and export of energy, depending on the energy provider.
 * @param price costs
 * @param energy price is related to delivered or consumed energy
 */
public record ImportExportChargeConfig (
    @JsonProperty("price") @JsonPropertyDescription("Price amount with currency") Money price,
    @JsonProperty("energy") @JsonPropertyDescription("Energy amount with unit") Energy energy
){ }