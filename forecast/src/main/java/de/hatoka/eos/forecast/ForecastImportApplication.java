package de.hatoka.eos.forecast;

import de.hatoka.eos.forecast.energycharts.EnergyChartsImporter;
import de.hatoka.eos.forecast.meteomedia.MeteoMediaWeatherForecastImporter;
import de.hatoka.eos.forecast.openmeteo.OpenMeteoWeatherForecastImporter;
import de.hatoka.eos.persistence.capi.weather.WeatherStation;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.time.ZonedDateTime;

@QuarkusMain
public class ForecastImportApplication implements QuarkusApplication
{
    private final Logger logger = LoggerFactory.getLogger(ForecastImportApplication.class);

    @Inject
    MeteoMediaWeatherForecastImporter meteoMediaImporter;

    @Inject
    OpenMeteoWeatherForecastImporter openMeteoImporter;

    @Inject
    EnergyChartsImporter energyChartsImporter;

    public static void main(String[] args)
    {
        Quarkus.run(ForecastImportApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception
    {
        logger.info("Starting weather forecast import...");
        try
        {
            ZonedDateTime startDate = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.of("UTC"));
            energyChartsImporter.importStockData(startDate);
            // Import from MeteoMedia (can only import data of today and tomorrow)
            logger.debug("Importing from MeteoMedia...");
            meteoMediaImporter.importWeatherForecast(WeatherStation.APOLDA, startDate);
            meteoMediaImporter.importWeatherForecast(WeatherStation.LEIPZIG_STADTWERKE, startDate);
            logger.info("Import from MeteoMedia finished.");

            // Import from OpenMeteo (can import up to 3 days)
            logger.debug("Importing from OpenMeteo...");
            openMeteoImporter.importWeatherForecast(WeatherStation.APOLDA, startDate);
            openMeteoImporter.importWeatherForecast(WeatherStation.LEIPZIG_STADTWERKE, startDate);
            logger.info("Import from OpenMeteo finished.");

            return 0;
        }
        catch (Exception e)
        {
            logger.error("Weather forecast import failed", e);
            return 1;
        }
    }
}
