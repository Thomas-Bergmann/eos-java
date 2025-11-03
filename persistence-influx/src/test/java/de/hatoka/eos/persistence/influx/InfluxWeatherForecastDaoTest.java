package de.hatoka.eos.persistence.influx;

import de.hatoka.eos.persistence.capi.MeteoMediaStation;
import de.hatoka.eos.persistence.capi.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.WeatherForecastPO;
import de.hatoka.eos.units.capi.Percentage;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for InfluxWeatherForecastDao.
 *
 * This test requires InfluxDB to be running (e.g., via docker-compose).
 * The test uses a dedicated test bucket to avoid interfering with production data.
 */
@QuarkusTest
class InfluxWeatherForecastDaoTest
{
    @Inject
    InfluxWeatherForecastDao dao;

    // Test data constants
    private static final ZonedDateTime TEST_TIME = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneId.of("UTC"));
    private static final Percentage TEST_SUN_PROBABILITY = new Percentage(0.75); // 75%
    private static final Percentage UPDATED_SUN_PROBABILITY = new Percentage(0.85); // 85%

    @AfterEach
    void tearDown()
    {
        dao.delete(getKey(TEST_TIME));
        dao.delete(getKey(TEST_TIME.plusHours(1)));
        dao.delete(getKey(TEST_TIME.minusHours(1)));
    }

    private static WeatherForecastPO createForecast(Percentage sunProbability)
    {
        WeatherForecastPO forecast = new WeatherForecastPO();
        forecast.setSunProbability(sunProbability);
        return forecast;
    }

    private static WeatherForecastKey getKey(ZonedDateTime time)
    {
        return WeatherForecastKey.valueOf(MeteoMediaStation.APOLDA, time);
    }

    @Test
    void shouldStoreAndRetrieveWeatherForecastData()
    {
        // Given
        WeatherForecastPO forecast = createForecast(TEST_SUN_PROBABILITY);

        // When
        dao.update(getKey(TEST_TIME), forecast);

        // Then
        WeatherForecastPO retrievedForecast = dao.get(getKey(TEST_TIME));

        assertNotNull(retrievedForecast, "Retrieved forecast should not be null");
        assertNotNull(retrievedForecast.getSunProbability(), "Sun probability should not be null");
        assertEquals(TEST_SUN_PROBABILITY.value(), retrievedForecast.getSunProbability().value(), 0.001,
                "Sun probability should match the stored value");
    }

    @Test
    void shouldUpdateExistingWeatherForecastData()
    {
        // Given - store initial data
        WeatherForecastPO initialForecast = createForecast(TEST_SUN_PROBABILITY);
        dao.update(getKey(TEST_TIME), initialForecast);

        // When - update with new data
        WeatherForecastPO updatedForecast = createForecast(UPDATED_SUN_PROBABILITY);
        dao.update(getKey(TEST_TIME), updatedForecast);

        // Then
        WeatherForecastPO retrievedForecast = dao.get(getKey(TEST_TIME));

        assertNotNull(retrievedForecast, "Retrieved forecast should not be null");
        assertNotNull(retrievedForecast.getSunProbability(), "Sun probability should not be null");
        assertEquals(UPDATED_SUN_PROBABILITY.value(), retrievedForecast.getSunProbability().value(), 0.001,
                "Sun probability should reflect the updated value");
    }

    @Test
    void shouldReturnEmptyForecastWhenNoDataExists()
    {
        // Given - a time point with no data
        ZonedDateTime nonExistentTime = TEST_TIME.plusDays(365);

        // When

        // Then
        assertNull(dao.get(getKey(nonExistentTime)), "Retrieved forecast should not be null");
    }

    @Test
    void shouldHandleMultipleTimePoints()
    {
        // Given - multiple forecasts at different times
        ZonedDateTime time1 = TEST_TIME;
        ZonedDateTime time2 = TEST_TIME.plusHours(1);
        ZonedDateTime time3 = TEST_TIME.plusHours(2);

        // When
        dao.update(getKey(time1), createForecast(new Percentage(0.5)));
        dao.update(getKey(time2), createForecast(new Percentage(0.7)));
        dao.update(getKey(time3), createForecast(new Percentage(0.9)));

        // Then
        assertEquals(0.5, dao.get(getKey(time1)).getSunProbability().value(), 0.001);
        assertEquals(0.7, dao.get(getKey(time2)).getSunProbability().value(), 0.001);
        assertEquals(0.9, dao.get(getKey(time3)).getSunProbability().value(), 0.001);

        // Clean up additional test data
        dao.delete(getKey(time2));
        dao.delete(getKey(time3));
    }

    @Test
    void shouldHandlePreciseTimeRetrieval()
    {
        // Given - forecast with precise timestamp
        ZonedDateTime preciseTime = TEST_TIME.truncatedTo(ChronoUnit.SECONDS);
        WeatherForecastPO forecast = createForecast(TEST_SUN_PROBABILITY);

        // When
        dao.update(getKey(preciseTime), forecast);

        // Then - should retrieve data even with slight time variations
        WeatherForecastPO retrieved = dao.get(getKey(preciseTime));

        assertNotNull(retrieved.getSunProbability());
        assertEquals(TEST_SUN_PROBABILITY.value(), retrieved.getSunProbability().value(), 0.001);
    }

    @Test
    void shouldHandleBoundaryPercentageValues()
    {
        // Test with 0% sun probability
        WeatherForecastPO forecast0 = createForecast(Percentage.ZERO);
        dao.update(getKey(TEST_TIME), forecast0);

        // Test with 100% sun probability
        ZonedDateTime time100 = TEST_TIME.plusMinutes(5);
        WeatherForecastPO forecast100 = createForecast(Percentage.ONE_HUNDRED);
        dao.update(getKey(time100), forecast100);

        // Verify boundary values
        assertEquals(0.0, dao.get(getKey(TEST_TIME)).getSunProbability().value(), 0.001);
        assertEquals(1.0, dao.get(getKey(time100)).getSunProbability().value(), 0.001);

        // Clean up
        dao.delete(getKey(time100));
    }

    @Test
    void shouldDeleteWeatherForecastData()
    {
        // Given - store some data first
        WeatherForecastPO forecast = createForecast(TEST_SUN_PROBABILITY);
        dao.update(getKey(TEST_TIME), forecast);

        // Verify data exists
        WeatherForecastPO retrievedBeforeDelete = dao.get(getKey(TEST_TIME));
        assertNotNull(retrievedBeforeDelete.getSunProbability(),
                "Data should exist before deletion");

        // When - delete the data
        dao.delete(getKey(TEST_TIME));

        // Then - data should no longer exist
        assertNull(dao.get(getKey(TEST_TIME)),"Data should not exist after deletion");
    }

    @Test
    void shouldDeleteOnlySpecificTimeData()
    {
        // Given - store data at multiple time points
        ZonedDateTime time1 = TEST_TIME;
        ZonedDateTime time2 = TEST_TIME.plusHours(1);
        ZonedDateTime time3 = TEST_TIME.plusHours(2);

        dao.update(getKey(time1), createForecast(new Percentage(0.3)));
        dao.update(getKey(time2), createForecast(new Percentage(0.6)));
        dao.update(getKey(time3), createForecast(new Percentage(0.9)));

        // When - delete only the middle data point
        dao.delete(getKey(time2));

        // Then - only the deleted data should be gone
        assertEquals(0.3, dao.get(getKey(time1)).getSunProbability().value(), 0.001);
        assertNull(dao.get(getKey(time2)), "Second data point should be deleted");
        assertEquals(0.9, dao.get(getKey(time3)).getSunProbability().value(), 0.001);

        // Clean up remaining data
        dao.delete(getKey(time1));
        dao.delete(getKey(time3));
    }

    @Test
    void shouldHandleDeleteOfNonExistentData()
    {
        // Given - a time point with no data
        ZonedDateTime nonExistentTime = TEST_TIME.plusDays(100);

        // When/Then - deleting non-existent data should not throw an exception
        assertDoesNotThrow(() -> dao.delete(getKey(nonExistentTime)),
                "Deleting non-existent data should not throw an exception");
    }

    @Test
    void shouldDeleteOnlyStation()
    {
        // Given - store data at multiple time points
        WeatherForecastKey leipzigKey = WeatherForecastKey.valueOf(MeteoMediaStation.LEIPZIG_STADTWERKE, TEST_TIME);
        dao.update(getKey(TEST_TIME), createForecast(new Percentage(0.3)));
        dao.update(leipzigKey, createForecast(new Percentage(0.6)));

        // When - delete only one station
        dao.delete(getKey(TEST_TIME));

        // Then - only the deleted data should be gone
        assertNull(dao.get(getKey(TEST_TIME)), "one data point should be deleted");
        assertNotNull(dao.get(leipzigKey), "the other data point should not be deleted");
        assertEquals(0.6, dao.get(leipzigKey).getSunProbability().value(), 0.001);

        dao.delete(leipzigKey);
    }
}

