package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

public record CsvPriceConfig(
    @JsonProperty("currency")
    @JsonPropertyDescription("Currency code for CSV price data (e.g., EUR, USD)")
    String currency,

    @JsonProperty("exportCharge")
    @JsonPropertyDescription("Export charge configuration (price per energy unit)")
    ImportExportChargeConfig exportCharge,

    @JsonProperty("importCharge")
    @JsonPropertyDescription("Import charge configuration (price per energy unit)")
    ImportExportChargeConfig importCharge,

    @JsonProperty("resource")
    @JsonPropertyDescription("List of CSV resource files containing price data")
    List<String> resource,
    @JsonProperty("gridConfig")
    @JsonPropertyDescription("Grid configuration with fallback pricing")
    GridConfig gridConfig
) { }