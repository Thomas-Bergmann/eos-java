package de.hatoka.eos.forecast;

import de.hatoka.eos.persistence.capi.WeatherStation;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@QuarkusMain
public class ForecastImportApplication implements QuarkusApplication
{
    private static final Logger logger = LoggerFactory.getLogger(ForecastImportApplication.class);

    @Inject
    MeteoMediaWeatherForecastImporter meteoMediaImporter;

    @Inject
    OpenMeteoWeatherForecastImporter openMeteoImporter;

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
            // Import from MeteoMedia (can only import data of today and tomorrow)
            logger.debug("Importing from MeteoMedia...");
            meteoMediaImporter.importWeatherForecast(WeatherStation.APOLDA);
            meteoMediaImporter.importWeatherForecast(WeatherStation.LEIPZIG_STADTWERKE);
            logger.info("Import from MeteoMedia finished.");

            // Import from OpenMeteo (can import up to 3 days)
            logger.debug("Importing from OpenMeteo...");
            openMeteoImporter.importWeatherForecast(WeatherStation.APOLDA);
            openMeteoImporter.importWeatherForecast(WeatherStation.LEIPZIG_STADTWERKE);
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
