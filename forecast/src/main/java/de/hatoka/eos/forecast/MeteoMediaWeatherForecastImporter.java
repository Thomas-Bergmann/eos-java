package de.hatoka.eos.forecast;

import de.hatoka.eos.persistence.capi.WeatherStation;
import de.hatoka.eos.persistence.capi.WeatherDataSource;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Downloads weather forecast PNG images from MeteoMedia external services and processes them to extract sunshine duration data, which is then stored in the
 * database.
 */
@Singleton
public class MeteoMediaWeatherForecastImporter extends AbstractWeatherForecastImporter
{
    private static final String URI_FORMAT = "https://wetterstationen.meteomedia.de/messnetz/vorhersagegrafik/%s.png";

    @Inject
    MeteoMediaWeatherSunshineDurationConverter converter;

    @Override
    protected WeatherDataSource getSource()
    {
        return WeatherDataSource.METEOMEDIA;
    }

    @Override
    protected Map<ZonedDateTime, Integer> downloadAndProcessWeatherData(WeatherStation station, ZonedDateTime startDate) 
            throws IOException, InterruptedException
    {
        // Download the PNG image
        byte[] imageData = downloadImage(URI.create(URI_FORMAT.formatted(station.getStationNumber())));
        logger.debug("Downloaded {} bytes of image data", imageData.length);

        // Save to temporary file for processing
        Path tempFile = saveTempImage(imageData, startDate);

        try
        {
            // Process the image and extract sunshine data
            return converter.extractSunshineDuration(converter.loadImageFromFile(tempFile.toFile()), startDate);
        }
        finally
        {
            // Clean up temporary file
            Files.deleteIfExists(tempFile);
        }
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
}
