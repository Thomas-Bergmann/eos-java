package de.hatoka.eos.persistence.influx;

import de.hatoka.eos.persistence.capi.WeatherForecastPO;
import de.hatoka.eos.units.capi.Percentage;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.time.ZoneId;
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
        dao.delete(TEST_TIME);
        dao.delete(TEST_TIME.plusHours(1));
        dao.delete(TEST_TIME.minusHours(1));
    }

    @Test
    void shouldStoreAndRetrieveWeatherForecastData()
    {
        // Given
        WeatherForecastPO forecast = new WeatherForecastPO();
        forecast.setSunProbability(TEST_SUN_PROBABILITY);

        // When
        dao.update(TEST_TIME, forecast);

        // Then
        WeatherForecastPO retrievedForecast = dao.get(TEST_TIME);

        assertNotNull(retrievedForecast, "Retrieved forecast should not be null");
        assertNotNull(retrievedForecast.getSunProbability(), "Sun probability should not be null");
        assertEquals(TEST_SUN_PROBABILITY.value(), retrievedForecast.getSunProbability().value(), 0.001,
                "Sun probability should match the stored value");
    }

    @Test
    void shouldUpdateExistingWeatherForecastData()
    {
        // Given - store initial data
        WeatherForecastPO initialForecast = new WeatherForecastPO();
        initialForecast.setSunProbability(TEST_SUN_PROBABILITY);
        dao.update(TEST_TIME, initialForecast);

        // When - update with new data
        WeatherForecastPO updatedForecast = new WeatherForecastPO();
        updatedForecast.setSunProbability(UPDATED_SUN_PROBABILITY);
        dao.update(TEST_TIME, updatedForecast);

        // Then
        WeatherForecastPO retrievedForecast = dao.get(TEST_TIME);

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
        assertNull(dao.get(nonExistentTime), "Retrieved forecast should not be null");
    }

    @Test
    void shouldHandleMultipleTimePoints()
    {
        // Given - multiple forecasts at different times
        ZonedDateTime time1 = TEST_TIME;
        ZonedDateTime time2 = TEST_TIME.plusHours(1);
        ZonedDateTime time3 = TEST_TIME.plusHours(2);

        WeatherForecastPO forecast1 = new WeatherForecastPO();
        forecast1.setSunProbability(new Percentage(0.5));

        WeatherForecastPO forecast2 = new WeatherForecastPO();
        forecast2.setSunProbability(new Percentage(0.7));

        WeatherForecastPO forecast3 = new WeatherForecastPO();
        forecast3.setSunProbability(new Percentage(0.9));

        // When
        dao.update(time1, forecast1);
        dao.update(time2, forecast2);
        dao.update(time3, forecast3);

        // Then
        WeatherForecastPO retrieved1 = dao.get(time1);
        WeatherForecastPO retrieved2 = dao.get(time2);
        WeatherForecastPO retrieved3 = dao.get(time3);

        assertEquals(0.5, retrieved1.getSunProbability().value(), 0.001);
        assertEquals(0.7, retrieved2.getSunProbability().value(), 0.001);
        assertEquals(0.9, retrieved3.getSunProbability().value(), 0.001);

        // Clean up additional test data
        dao.delete(time2);
        dao.delete(time3);
    }

    @Test
    void shouldHandlePreciseTimeRetrieval()
    {
        // Given - forecast with precise timestamp
        ZonedDateTime preciseTime = TEST_TIME.truncatedTo(ChronoUnit.SECONDS);
        WeatherForecastPO forecast = new WeatherForecastPO();
        forecast.setSunProbability(TEST_SUN_PROBABILITY);

        // When
        dao.update(preciseTime, forecast);

        // Then - should retrieve data even with slight time variations
        WeatherForecastPO retrieved = dao.get(preciseTime);

        assertNotNull(retrieved.getSunProbability());
        assertEquals(TEST_SUN_PROBABILITY.value(), retrieved.getSunProbability().value(), 0.001);
    }

    @Test
    void shouldHandleBoundaryPercentageValues()
    {
        // Test with 0% sun probability
        WeatherForecastPO forecast0 = new WeatherForecastPO();
        forecast0.setSunProbability(Percentage.ZERO);
        dao.update(TEST_TIME, forecast0);

        // Test with 100% sun probability
        ZonedDateTime time100 = TEST_TIME.plusMinutes(5);
        WeatherForecastPO forecast100 = new WeatherForecastPO();
        forecast100.setSunProbability(Percentage.ONE_HUNDRED);
        dao.update(time100, forecast100);

        // Verify boundary values
        WeatherForecastPO retrieved0 = dao.get(TEST_TIME);
        WeatherForecastPO retrieved100 = dao.get(time100);

        assertEquals(0.0, retrieved0.getSunProbability().value(), 0.001);
        assertEquals(1.0, retrieved100.getSunProbability().value(), 0.001);

        // Clean up
        dao.delete(time100);
    }

    @Test
    void shouldDeleteWeatherForecastData()
    {
        // Given - store some data first
        WeatherForecastPO forecast = new WeatherForecastPO();
        forecast.setSunProbability(TEST_SUN_PROBABILITY);
        dao.update(TEST_TIME, forecast);

        // Verify data exists
        WeatherForecastPO retrievedBeforeDelete = dao.get(TEST_TIME);
        assertNotNull(retrievedBeforeDelete.getSunProbability(),
                "Data should exist before deletion");

        // When - delete the data
        dao.delete(TEST_TIME);

        // Then - data should no longer exist
        assertNull(dao.get(TEST_TIME),"Data should not exist after deletion");
    }

    @Test
    void shouldDeleteOnlySpecificTimeData()
    {
        // Given - store data at multiple time points
        ZonedDateTime time1 = TEST_TIME;
        ZonedDateTime time2 = TEST_TIME.plusHours(1);
        ZonedDateTime time3 = TEST_TIME.plusHours(2);

        WeatherForecastPO forecast1 = new WeatherForecastPO();
        forecast1.setSunProbability(new Percentage(0.3));

        WeatherForecastPO forecast2 = new WeatherForecastPO();
        forecast2.setSunProbability(new Percentage(0.6));

        WeatherForecastPO forecast3 = new WeatherForecastPO();
        forecast3.setSunProbability(new Percentage(0.9));

        dao.update(time1, forecast1);
        dao.update(time2, forecast2);
        dao.update(time3, forecast3);

        // When - delete only the middle data point
        dao.delete(time2);

        // Then - only the deleted data should be gone
        WeatherForecastPO retrieved1 = dao.get(time1);
        WeatherForecastPO retrieved2 = dao.get(time2);
        WeatherForecastPO retrieved3 = dao.get(time3);

        assertNotNull(retrieved1.getSunProbability(), "First data point should still exist");
        assertNull(retrieved2, "Second data point should be deleted");
        assertNotNull(retrieved3.getSunProbability(), "Third data point should still exist");

        assertEquals(0.3, retrieved1.getSunProbability().value(), 0.001);
        assertEquals(0.9, retrieved3.getSunProbability().value(), 0.001);

        // Clean up remaining data
        dao.delete(time1);
        dao.delete(time3);
    }

    @Test
    void shouldHandleDeleteOfNonExistentData()
    {
        // Given - a time point with no data
        ZonedDateTime nonExistentTime = TEST_TIME.plusDays(100);

        // When/Then - deleting non-existent data should not throw an exception
        assertDoesNotThrow(() -> dao.delete(nonExistentTime),
                "Deleting non-existent data should not throw an exception");
    }
}

