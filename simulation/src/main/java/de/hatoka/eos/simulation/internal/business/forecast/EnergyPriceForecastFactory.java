package de.hatoka.eos.simulation.internal.business.forecast;

import de.hatoka.eos.persistence.capi.energystock.EnergyStockDao;
import de.hatoka.eos.simulation.capi.business.config.GridConfig;
import de.hatoka.eos.simulation.capi.business.forecast.EnergyPriceForecast;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Singleton
public class EnergyPriceForecastFactory
{
    @Inject
    private EnergyStockDao dao;

    @Inject
    private DaoEnergyPriceForecast daoEnergyPriceForecast;

    /**
     * @return the configured price forecast
     */
    public EnergyPriceForecast getEnergyPriceProvider(GridConfig gridConfig)
    {
        return switch(gridConfig.type())
        {
            case DAO -> daoEnergyPriceForecast;
            case FLAT -> new FlatPriceService(gridConfig.flatPriceConfig());
        };
    }

}
