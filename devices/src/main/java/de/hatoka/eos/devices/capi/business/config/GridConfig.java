package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.devices.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.devices.internal.business.forecast.FlatPriceService;
import de.hatoka.eos.devices.internal.business.forecast.CsvStockEnergyPriceProvider;

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