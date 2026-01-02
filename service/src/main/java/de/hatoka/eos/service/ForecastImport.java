package de.hatoka.eos.service;

import de.hatoka.eos.forecast.energycharts.EnergyChartsImporter;
import de.hatoka.eos.forecast.openmeteo.OpenMeteoWeatherForecastImporter;
import de.hatoka.eos.persistence.capi.weather.WeatherStation;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@Singleton
public class ForecastImport
{
    private final Logger logger = LoggerFactory.getLogger(ForecastImport.class);

    @Inject
    OpenMeteoWeatherForecastImporter openMeteoImporter;
    @Inject
    EnergyChartsImporter energyChartsImporter;

    public void run() throws Exception
    {
        logger.info("Importing stock from Energy Charts...");
        ZonedDateTime startDate = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        energyChartsImporter.importStockData(startDate);
        logger.info("Importing from Energy Charts finished.");

        // Import from OpenMeteo (can import up to 3 days)
        logger.debug("Importing from weather from OpenMeteo...");
        openMeteoImporter.importWeatherForecast(WeatherStation.APOLDA, startDate);
        openMeteoImporter.importWeatherForecast(WeatherStation.LEIPZIG_STADTWERKE, startDate);
        logger.info("Import from OpenMeteo finished.");
    }
}
