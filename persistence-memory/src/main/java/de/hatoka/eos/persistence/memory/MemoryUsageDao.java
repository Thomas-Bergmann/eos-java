package de.hatoka.eos.persistence.memory;

import de.hatoka.eos.persistence.capi.usage.UsageDao;
import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import jakarta.inject.Singleton;

import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation for Telemetry data
 */
@Singleton
public class MemoryUsageDao implements UsageDao
{
    private static final ConcurrentHashMap<UsageKey, UsagePO> STORAGE = new ConcurrentHashMap<>();

    @Override
    public void update(UsageKey key, UsagePO data)
    {
        STORAGE.put(key, data);
    }

    @Override
    public void delete(UsageKey key)
    {
        STORAGE.remove(key);
    }

    @Override
    public UsagePO get(UsageKey key)
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

