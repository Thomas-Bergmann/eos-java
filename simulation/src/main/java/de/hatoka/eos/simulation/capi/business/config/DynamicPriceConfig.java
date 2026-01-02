package de.hatoka.eos.simulation.capi.business.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;

import java.util.List;

/**
 * The CsvPriceConfig declares, how stock prices are provided via CSV files
 * @param currency currency of stock price
 * @param exportCharge additional charge for exporting energy to the grid
 * @param importCharge additional charge for importing energy from grid
 */
public record DynamicPriceConfig(
    @JsonProperty("currency")
    @JsonPropertyDescription("Currency code for CSV price data (e.g., EUR, USD)")
    String currency,

    @JsonProperty("exportCharge")
    @JsonPropertyDescription("Export charge configuration (price per energy unit)")
    ImportExportChargeConfig exportCharge,

    @JsonProperty("importCharge")
    @JsonPropertyDescription("Import charge configuration (price per energy unit)")
    ImportExportChargeConfig importCharge
) { }