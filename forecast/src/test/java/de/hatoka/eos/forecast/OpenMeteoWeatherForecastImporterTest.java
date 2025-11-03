package de.hatoka.eos.forecast;

import de.hatoka.eos.persistence.capi.MeteoMediaStation;
import de.hatoka.eos.persistence.capi.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.WeatherForecastPO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
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
        ZonedDateTime testDate = importer.importWeatherForecast(MeteoMediaStation.APOLDA);
        
        // Verify that data was imported successfully
        assertNotNull(testDate, "Import should return a valid start date");
        
        // Check if data was stored in the database (test the next hour)
        WeatherForecastPO retrieved = weatherDao.get(WeatherForecastKey.valueOf(MeteoMediaStation.APOLDA, testDate.plusHours(1)));
        assertNotNull(retrieved, "Weather data should be stored in database");
        assertNotNull(retrieved.getSunProbability(), "Sun probability should not be null");
        
        // Verify the sun probability is within valid range (0.0 to 1.0)
        double sunProb = retrieved.getSunProbability().value();
        assertTrue(sunProb >= 0.0 && sunProb <= 1.0, 
                   "Sun probability should be between 0.0 and 1.0, but was: " + sunProb);
        
        // Test importing for a different station
        ZonedDateTime testDate2 = importer.importWeatherForecast(MeteoMediaStation.LEIPZIG_STADTWERKE);
        assertNotNull(testDate2, "Import for Leipzig should also return a valid start date");
    }

    @Test
    void testStationCoordinates()
    {
        // Verify that our stations have valid coordinates for OpenMeteo API
        assertNotEquals(0.0, MeteoMediaStation.APOLDA.getLatitude(), "APOLDA should have valid latitude");
        assertNotEquals(0.0, MeteoMediaStation.APOLDA.getLongitude(), "APOLDA should have valid longitude");
        
        assertNotEquals(0.0, MeteoMediaStation.LEIPZIG_STADTWERKE.getLatitude(), "LEIPZIG_STADTWERKE should have valid latitude");
        assertNotEquals(0.0, MeteoMediaStation.LEIPZIG_STADTWERKE.getLongitude(), "LEIPZIG_STADTWERKE should have valid longitude");
        
        // Check that coordinates are reasonable for Germany
        assertTrue(MeteoMediaStation.APOLDA.getLatitude() > 47.0 && MeteoMediaStation.APOLDA.getLatitude() < 55.0,
                   "APOLDA latitude should be within Germany bounds");
        assertTrue(MeteoMediaStation.APOLDA.getLongitude() > 5.0 && MeteoMediaStation.APOLDA.getLongitude() < 15.0,
                   "APOLDA longitude should be within Germany bounds");
    }
}