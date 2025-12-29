package de.hatoka.eos.persistence.memory;

import de.hatoka.eos.persistence.capi.weather.WeatherDataSource;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastPO;
import de.hatoka.eos.persistence.capi.weather.WeatherStation;
import de.hatoka.eos.units.capi.Percentage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MemoryWeatherForecastDaoTest
{
    private MemoryWeatherForecastDao dao;

    @BeforeEach
    void setUp()
    {
        dao = new MemoryWeatherForecastDao();
    }

    @Test
    void testUpdateAndGet()
    {
        // Given
        WeatherForecastKey key = WeatherForecastKey.valueOf(
            WeatherStation.APOLDA,
            ZonedDateTime.now(),
            WeatherDataSource.OPENMETEO
        );
        WeatherForecastPO data = new WeatherForecastPO();
        data.setSunProbability(new Percentage(0.755));

        // When
        dao.update(key, data);
        WeatherForecastPO result = dao.get(key);

        // Then
        assertNotNull(result);
        assertEquals(0.755, result.getSunProbability().value(), 0.001);
    }

    @Test
    void testGetNonExistent()
    {
        // Given
        WeatherForecastKey key = WeatherForecastKey.valueOf(
            WeatherStation.APOLDA,
            ZonedDateTime.now(),
            WeatherDataSource.OPENMETEO
        );

        // When
        WeatherForecastPO result = dao.get(key);

        // Then
        assertNull(result);
    }

    @Test
    void testDelete()
    {
        // Given
        WeatherForecastKey key = WeatherForecastKey.valueOf(
            WeatherStation.APOLDA,
            ZonedDateTime.now(),
            WeatherDataSource.OPENMETEO
        );
        WeatherForecastPO data = new WeatherForecastPO();
        data.setSunProbability(new Percentage(0.755));
        dao.update(key, data);

        // When
        dao.delete(key);
        WeatherForecastPO result = dao.get(key);

        // Then
        assertNull(result);
    }

    @Test
    void testClear()
    {
        // Given
        WeatherForecastKey key1 = WeatherForecastKey.valueOf(
            WeatherStation.APOLDA,
            ZonedDateTime.now(),
            WeatherDataSource.OPENMETEO
        );
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(
            WeatherStation.APOLDA,
            ZonedDateTime.now().plusHours(1),
            WeatherDataSource.OPENMETEO
        );
        WeatherForecastPO data = new WeatherForecastPO();
        data.setSunProbability(new Percentage(0.755));
        dao.update(key1, data);
        dao.update(key2, data);

        // When
        dao.clear();

        // Then
        assertNull(dao.get(key1));
        assertNull(dao.get(key2));
    }
}

