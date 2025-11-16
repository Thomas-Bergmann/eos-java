package de.hatoka.eos.simulation.capi.business.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.simulation.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.simulation.internal.business.forecast.FlatPriceService;
import de.hatoka.eos.simulation.internal.business.forecast.CsvStockEnergyPriceProvider;

/**
 * The GridConfig is the container for grid pricing configuration. The type can be CSV or FLAT and selects the related configuration.
 * @param type CSV or FLAT
 * @param csvPriceProvider stock price configuration
 * @param flatPriceConfig flat price configuration
 */
public record GridConfig (
    @JsonProperty("type")
    @JsonPropertyDescription("select csvPriceProvider or flatPriceProvider")
    GridPriceConfigType type,

    @JsonProperty("csvPriceProvider")
    @JsonPropertyDescription("CSV-based price provider configuration")
    CsvPriceConfig csvPriceProvider,

    @JsonProperty("flatPriceProvider")
    @JsonPropertyDescription("Flat price provider configuration")
    FlatPriceConfig flatPriceConfig
)
{
    /**
     * @return the configured price forecast
     */
    @JsonIgnore
    public EnergyPriceForecast getEnergyPriceProvider()
    {
        return switch(type)
        {
            case CSV -> new CsvStockEnergyPriceProvider(csvPriceProvider);
            case FLAT -> new FlatPriceService(flatPriceConfig);
        };
    }
}