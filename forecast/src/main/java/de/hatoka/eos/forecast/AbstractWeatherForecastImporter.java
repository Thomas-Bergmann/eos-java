package de.hatoka.eos.forecast;

import de.hatoka.eos.persistence.capi.weather.WeatherStation;
import de.hatoka.eos.persistence.capi.weather.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastPO;
import de.hatoka.eos.persistence.capi.weather.WeatherDataSource;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

/**
 * Abstract base class for weather forecast importers that provides common functionality
 * for downloading, processing, and storing weather forecast data.
 */
public abstract class AbstractWeatherForecastImporter
{
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    protected WeatherForcastDAO weatherDao;

    /**
     * Gets the source identifier for this importer.
     * 
     * @return the source identifier (e.g., WeatherDataSource.METEOMEDIA, WeatherDataSource.OPENMETEO)
     */
    protected abstract WeatherDataSource getSource();

    /**
     * Imports weather forecast data for the specified station.
     * 
     * @param station the weather station to import data for
     * @return the start date of the imported forecast data
     * @throws IOException if there's an error downloading or processing the data
     * @throws InterruptedException if the operation is interrupted
     */
    public ZonedDateTime importWeatherForecast(WeatherStation station, ZonedDateTime startDate) throws IOException, InterruptedException
    {
        logger.info("Starting weather forecast import for station: {}", station.name());

        try
        {
            // Download and process the weather data (implementation specific)
            Map<ZonedDateTime, Integer> sunshineDurationPerHour = downloadAndProcessWeatherData(station, startDate);

            logger.info("Extracted sunshine data for {} hours", sunshineDurationPerHour.size());

            // Store the data in the database
            storeSunshineData(sunshineDurationPerHour, station);

            logger.info("Successfully imported weather forecast data for {}", startDate);
        }
        catch (Exception e)
        {
            logger.error("Failed to import weather forecast for station " + station.name(), e);
            throw e;
        }

        return startDate;
    }

    /**
     * Downloads and processes weather data from the specific weather service.
     * This method must be implemented by subclasses to handle their specific data sources.
     * 
     * @param station the weather station to get data for
     * @param startDate the start date for the forecast
     * @return map of datetime to sunshine minutes per hour
     * @throws IOException if there's an error downloading or processing the data
     * @throws InterruptedException if the operation is interrupted
     */
    protected abstract Map<ZonedDateTime, Integer> downloadAndProcessWeatherData(WeatherStation station, ZonedDateTime startDate) 
            throws IOException, InterruptedException;

    /**
     * Stores sunshine duration data in the database.
     * Converts sunshine minutes (0-60) to sun probability (0.0-1.0) and stores in the database.
     * 
     * @param sunshineDurationPerHour map of datetime to sunshine minutes
     * @param station the weather station this data belongs to
     */
    protected void storeSunshineData(Map<ZonedDateTime, Integer> sunshineDurationPerHour, WeatherStation station)
    {
        for (Map.Entry<ZonedDateTime, Integer> entry : sunshineDurationPerHour.entrySet())
        {
            ZonedDateTime dateTime = entry.getKey();
            Integer sunshineMinutes = entry.getValue();

            // Convert sunshine minutes (0-60) to probability (0.0-1.0)
            double sunProbability = Math.min(1.0, sunshineMinutes / 60.0);

            WeatherForecastPO forecast = new WeatherForecastPO();
            forecast.setSunProbability(new Percentage(sunProbability));

            try
            {
                weatherDao.update(new WeatherForecastKey(station.name(), dateTime, getSource()), forecast);
                logger.debug("Stored weather data for {}: {}% sun probability", dateTime, Math.round(sunProbability * 100));
            }
            catch(Exception e)
            {
                logger.error("Failed to store weather data for " + dateTime, e);
            }
        }
    }
}