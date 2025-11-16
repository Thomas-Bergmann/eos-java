package de.hatoka.eos.forecast.meteomedia;

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
        ZonedDateTime startDate = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        ZonedDateTime testDate = importer.importWeatherForecast(WeatherStation.APOLDA, startDate);
        WeatherForecastPO retrieved = weatherDao.get(WeatherForecastKey.valueOf(WeatherStation.APOLDA, testDate.plusHours(1), WeatherDataSource.METEOMEDIA));
        assertNotNull(retrieved);
        assertNotNull(retrieved.getSunProbability());
    }
}
