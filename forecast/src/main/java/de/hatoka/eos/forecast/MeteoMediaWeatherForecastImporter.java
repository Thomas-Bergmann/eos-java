package de.hatoka.eos.forecast;

import de.hatoka.eos.persistence.capi.MeteoMediaStation;
import de.hatoka.eos.persistence.capi.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.WeatherForecastPO;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Downloads weather forecast PNG images from external services and processes them to extract sunshine duration data, which is then stored in the
 * database.
 */
@Singleton
public class MeteoMediaWeatherForecastImporter
{
    private static final Logger logger = LoggerFactory.getLogger(MeteoMediaWeatherForecastImporter.class);
    private static final String URI_FORMAT = "https://wetterstationen.meteomedia.de/messnetz/vorhersagegrafik/%s.png";

    @Inject
    MeteoMediaWeatherSunshineDurationConverter converter;
    @Inject
    WeatherForcastDAO weatherDao;

    public ZonedDateTime importWeatherForecast(MeteoMediaStation station) throws IOException, InterruptedException
    {
        ZonedDateTime startDate = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.of("UTC"));
        logger.info("Starting weather forecast import for station: {}", station.name());

        // Download the PNG image
        byte[] imageData = downloadImage(URI.create(URI_FORMAT.formatted(station.getStationNumber())));
        logger.debug("Downloaded {} bytes of image data", imageData.length);

        // Save to temporary file for processing
        Path tempFile = saveTempImage(imageData, startDate);

        try
        {
            // Process the image and extract sunshine data
            Map<ZonedDateTime, Integer> sunshineDurationPerHour = converter.extractSunshineDuration(converter.loadImageFromFile(tempFile.toFile()),
                            startDate);

            logger.info("Extracted sunshine data for {} hours", sunshineDurationPerHour.size());

            // Store the data in the database
            storeSunshineData(sunshineDurationPerHour, station);

            logger.info("Successfully imported weather forecast data for {}", startDate);
        }
        finally
        {
            // Clean up temporary file
            Files.deleteIfExists(tempFile);
        }
        return startDate;
    }

    private byte[] downloadImage(URI imageUrl) throws IOException, InterruptedException
    {
        try (HttpClient httpClient = HttpClient.newBuilder()
                                               .connectTimeout(Duration.ofSeconds(30))
                                               .followRedirects(HttpClient.Redirect.NORMAL)
                                               .build())
        {
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(imageUrl)
                                             .timeout(Duration.ofSeconds(30))
                                             .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                                             .header("Accept", "image/png,image/*;q=0.9,*/*;q=0.8")
                                             .GET()
                                             .build();

            logger.debug("Sending HTTP request to: {}", imageUrl);

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200)
            {
                throw new IOException("HTTP request failed with status code: " + response.statusCode());
            }
            return response.body();
        }
    }

    private Path saveTempImage(byte[] imageData, ZonedDateTime startDate) throws IOException
    {
        String filename = "weather_forecast_" + startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmm")) + ".png";

        Path tempFile = Files.createTempFile("weather_", filename);
        Files.write(tempFile, imageData, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);

        logger.debug("Saved temporary image file: {}", tempFile);
        return tempFile;
    }

    private void storeSunshineData(Map<ZonedDateTime, Integer> sunshineDurationPerHour, MeteoMediaStation station)
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
                weatherDao.update(new WeatherForecastKey(station.name(), dateTime), forecast);
                logger.debug("Stored weather data for {}: {}% sun probability", dateTime, Math.round(sunProbability * 100));
            }
            catch(Exception e)
            {
                logger.error("Failed to store weather data for " + dateTime, e);
            }
        }
    }
}
