package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Money;

public record ImportExportChargeConfig (
    @JsonProperty("price") @JsonPropertyDescription("Price amount with currency") Money price,
    @JsonProperty("energy") @JsonPropertyDescription("Energy amount with unit") Energy energy
){ }