package de.hatoka.eos.simulation.internal.business.forecast;

import de.hatoka.eos.simulation.capi.business.config.FlatPriceConfig;
import de.hatoka.eos.simulation.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.units.capi.Money;

import java.time.ZonedDateTime;

/**
 * FlatPriceService normally used, if no dynamic prices are activated
 */
public class FlatPriceService implements EnergyPriceForecast
{
    public static final EnergyPriceForecast GERMAN_RESIDENTIAL = new FlatPriceService(FlatPriceConfig.GERMAN_RESIDENTIAL);

    private final Money importPrice;
    private final Money exportPrice;

    public FlatPriceService(FlatPriceConfig config)
    {
        this.importPrice = config.importPrice();
        this.exportPrice = config.exportPrice();
    }

    @Override
    public Money getImportPrice(ZonedDateTime time)
    {
        return importPrice;
    }

    @Override
    public Money getExportPrice(ZonedDateTime time)
    {
        return exportPrice;
    }
}
