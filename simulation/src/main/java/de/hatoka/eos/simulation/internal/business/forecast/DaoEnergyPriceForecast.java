package de.hatoka.eos.simulation.internal.business.forecast;

import de.hatoka.eos.persistence.capi.energystock.EnergyStockDao;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockKey;
import de.hatoka.eos.simulation.capi.business.config.GridConfig;
import de.hatoka.eos.simulation.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.units.capi.Money;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.ZonedDateTime;

@Singleton
public class DaoEnergyPriceForecast implements EnergyPriceForecast
{
    @Inject
    private EnergyStockDao stockDao;
    @Inject
    private GridConfig config;

    @Override
    public Money getImportPrice(ZonedDateTime time)
    {
        // config.dynamicPriceConfig().importCharge()
        return stockDao.get(new EnergyStockKey(time)).getDayAheadPrice();
    }

    @Override
    public Money getExportPrice(ZonedDateTime time)
    {
        return stockDao.get(new EnergyStockKey(time)).getDayAheadPrice();
    }
}
