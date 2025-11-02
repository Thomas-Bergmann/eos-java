package de.hatoka.eos.forecast;

import de.hatoka.eos.persistence.capi.MeteoMediaStation;
import de.hatoka.eos.persistence.capi.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.WeatherForecastPO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for WeatherForecastImporter functionality.
 * Note: This test requires network access to download actual weather images.
 * For CI/CD environments, you might want to mock the HTTP client or use test resources.
 */
@QuarkusTest
class MeteoMediaWeatherForecastImporterTest
{
    @Inject
    MeteoMediaWeatherForecastImporter importer;

    @Inject
    WeatherForcastDAO weatherDao;

    @Test
    void testImportFromValidUrl() throws IOException, InterruptedException
    {
        // this importer can only import data of today and tomorrow
        ZonedDateTime testDate = importer.importWeatherForecast(MeteoMediaStation.APOLDA);
        WeatherForecastPO retrieved = weatherDao.get(testDate.plusHours(1));
        assertNotNull(retrieved);
        assertNotNull(retrieved.getSunProbability());
        importer.importWeatherForecast(MeteoMediaStation.LEIPZIG_STADTWERKE);
    }
}
