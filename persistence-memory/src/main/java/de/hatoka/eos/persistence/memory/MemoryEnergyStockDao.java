package de.hatoka.eos.persistence.memory;

import de.hatoka.eos.persistence.capi.energystock.EnergyStockDao;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockKey;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockPO;
import jakarta.inject.Singleton;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation for EnergyStockDao
 */
@Singleton
public class MemoryEnergyStockDao implements EnergyStockDao
{
    private static final ConcurrentHashMap<EnergyStockKey, EnergyStockPO> STORAGE = new ConcurrentHashMap<>();

    @Override
    public void update(EnergyStockKey key, EnergyStockPO data)
    {
        STORAGE.put(key, data);
    }

    @Override
    public void delete(EnergyStockKey key)
    {
        STORAGE.remove(key);
    }

    @Override
    public EnergyStockPO get(EnergyStockKey key)
    {
        return STORAGE.get(key);
    }

    /**
     * Clear all data (useful for testing)
     */
    public void clear()
    {
        STORAGE.clear();
    }
}

