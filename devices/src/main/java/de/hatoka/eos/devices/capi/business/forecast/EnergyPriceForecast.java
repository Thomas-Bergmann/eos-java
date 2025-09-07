package de.hatoka.eos.devices.capi.business.forecast;

import de.hatoka.eos.devices.capi.units.Money;

import java.time.ZonedDateTime;

/**
 * EnergyPriceProvider provides prices for exported (feed-in) and imported energy of the grid.
 * The prices containing network charges; all prices are "positive" also if they represent costs.
 */
public interface EnergyPriceForecast
{
    /**
     * @param time time stamp
     * @return price to import/consumption of energy from the grid (price per kWH)
     */
    Money getImportPrice(ZonedDateTime time);

    /**
     * @param time time stamp
     * @return price to export/produced/feed-in of energy to the grid (price per kWH)
     */
    Money getExportPrice(ZonedDateTime time);
}
