package de.hatoka.eos.persistence.memory;

import de.hatoka.eos.persistence.capi.weather.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastPO;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory implementation for WeatherForecastDAO
 */
@Singleton
public class MemoryWeatherForecastDao implements WeatherForcastDAO
{
    private static final Map<WeatherForecastKey, WeatherForecastPO> STORAGE = new ConcurrentHashMap<>();

    @Override
    public void update(WeatherForecastKey key, WeatherForecastPO data)
    {
        STORAGE.put(key, data);
    }

    @Override
    public void delete(WeatherForecastKey key)
    {
        STORAGE.remove(key);
    }

    @Override
    public WeatherForecastPO get(WeatherForecastKey key)
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

