package de.hatoka.eos.forecast.openmeteo;

import de.hatoka.eos.persistence.capi.weather.WeatherStation;
import de.hatoka.eos.persistence.capi.weather.WeatherDataSource;
import de.hatoka.eos.persistence.capi.weather.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastPO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for OpenMeteoWeatherForecastImporter functionality.
 * Note: This test requires network access to download actual weather data from OpenMeteo API.
 * For CI/CD environments, you might want to mock the HTTP client or use test resources.
 */
@QuarkusTest
class OpenMeteoWeatherForecastImporterTest
{
    @Inject
    OpenMeteoWeatherForecastImporter importer;

    @Inject
    WeatherForcastDAO weatherDao;

    @Test
    void testImportFromOpenMeteoApi() throws IOException, InterruptedException
    {
        // Test importing data from OpenMeteo API
        ZonedDateTime startDate = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime testDate = importer.importWeatherForecast(WeatherStation.APOLDA, startDate);
        
        // Verify that data was imported successfully
        assertNotNull(testDate, "Import should return a valid start date");
        
        // Check if data was stored in the database (test the next hour)
        WeatherForecastPO retrieved = weatherDao.get(WeatherForecastKey.valueOf(WeatherStation.APOLDA, testDate.plusHours(1), WeatherDataSource.OPENMETEO));
        assertNotNull(retrieved, "Weather data should be stored in database");
        assertNotNull(retrieved.getSunProbability(), "Sun probability should not be null");
        
        // Verify the sun probability is within valid range (0.0 to 1.0)
        double sunProb = retrieved.getSunProbability().value();
        assertTrue(sunProb >= 0.0 && sunProb <= 1.0, 
                   "Sun probability should be between 0.0 and 1.0, but was: " + sunProb);
    }

    @Test
    void testStationCoordinates()
    {
        // Test that all weather stations have valid coordinates
        for (WeatherStation station : WeatherStation.values())
        {
            assertTrue(station.getLatitude() >= -90.0 && station.getLatitude() <= 90.0,
                       "Station " + station.name() + " has invalid latitude: " + station.getLatitude());
            assertTrue(station.getLongitude() >= -180.0 && station.getLongitude() <= 180.0,
                       "Station " + station.name() + " has invalid longitude: " + station.getLongitude());
            assertNotNull(station.getStationNumber(),
                          "Station " + station.name() + " should have a station number");
            assertFalse(station.getStationNumber().isEmpty(),
                        "Station " + station.name() + " should have a non-empty station number");
        }
    }
}