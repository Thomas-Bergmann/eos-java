package de.hatoka.eos.forecast.openmeteo;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hatoka.eos.forecast.AbstractWeatherForecastImporter;
import de.hatoka.eos.persistence.capi.weather.WeatherStation;
import de.hatoka.eos.persistence.capi.weather.WeatherDataSource;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Downloads weather forecast data from OpenMeteo API and processes it to extract sunshine duration data.
 *
 * OpenMeteo API Documentation: https://open-meteo.com/en/docs
 *
 * Example API call: https://api.open-meteo.com/v1/forecast?latitude=51.0262&longitude=11.5164&hourly=sunshine_duration&forecast_days=3&timezone=UTC
 */
@Singleton
public class OpenMeteoWeatherForecastImporter extends AbstractWeatherForecastImporter
{
    private static final String API_BASE_URL = "https://api.open-meteo.com/v1/forecast";
    private static final String API_QUERY = API_BASE_URL + "?latitude=%.4f&longitude=%.4f&hourly=sunshine_duration&forecast_days=%s&timezone=%s";

    private static final String FORECAST_DAYS = "3";
    private static final String TIMEZONE = "UTC";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected WeatherDataSource getSource()
    {
        return WeatherDataSource.OPENMETEO;
    }

    @Override
    protected Map<ZonedDateTime, Integer> downloadAndProcessWeatherData(WeatherStation station, ZonedDateTime startDate)
                    throws IOException, InterruptedException
    {
        // Build the OpenMeteo API URL
        String apiUrl = buildApiUrl(station);
        logger.debug("Requesting OpenMeteo API: {}", apiUrl);

        // Download the JSON data from OpenMeteo API
        String jsonResponse = downloadJsonData(URI.create(apiUrl));
        logger.debug("Downloaded {} characters of JSON data", jsonResponse.length());

        // Parse the JSON response
        OpenMeteoResponse response = objectMapper.readValue(jsonResponse, OpenMeteoResponse.class);

        // Convert the response to our internal format
        return convertToSunshineDurationMap(response);
    }

    /**
     * Builds the OpenMeteo API URL for the given station.
     *
     * @param station the weather station
     * @return the complete API URL
     */
    private String buildApiUrl(WeatherStation station)
    {
        return API_QUERY.formatted(station.getLatitude(), station.getLongitude(), FORECAST_DAYS, TIMEZONE);
    }

    /**
     * Downloads JSON data from the OpenMeteo API.
     *
     * @param apiUrl the API URL to call
     * @return the JSON response as a string
     * @throws IOException if the HTTP request fails
     * @throws InterruptedException if the request is interrupted
     */
    private String downloadJsonData(URI apiUrl) throws IOException, InterruptedException
    {
        try (HttpClient httpClient = HttpClient.newBuilder()
                                               .connectTimeout(Duration.ofSeconds(30))
                                               .followRedirects(HttpClient.Redirect.NORMAL)
                                               .build())
        {
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(apiUrl)
                                             .timeout(Duration.ofSeconds(30))
                                             .header("User-Agent", "EOS-Weather-Forecast-Importer/1.0")
                                             .header("Accept", "application/json")
                                             .GET()
                                             .build();

            logger.debug("Sending HTTP request to OpenMeteo API: {}", apiUrl);

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
            {
                throw new IOException("OpenMeteo API request failed with status code: " + response.statusCode() + ", response: " + response.body());
            }

            return response.body();
        }
    }

    /**
     * Converts the OpenMeteo API response to our internal sunshine duration format.
     *
     * @param response the OpenMeteo API response
     * @return map of datetime to sunshine duration in minutes
     */
    private Map<ZonedDateTime, Integer> convertToSunshineDurationMap(OpenMeteoResponse response)
    {
        Map<ZonedDateTime, Integer> result = new HashMap<>();

        if (response.getHourly() == null)
        {
            logger.warn("No hourly data in OpenMeteo response");
            return result;
        }

        List<String> times = response.getHourly().getTime();
        List<Double> sunshineDurations = response.getHourly().getSunshineDuration();

        if (times == null || sunshineDurations == null)
        {
            logger.warn("Missing time or sunshine_duration data in OpenMeteo response");
            return result;
        }

        if (times.size() != sunshineDurations.size())
        {
            logger.warn("Time and sunshine_duration arrays have different sizes: {} vs {}", times.size(), sunshineDurations.size());
            return result;
        }

        // Parse each time/sunshine pair
        for (int i = 0; i < times.size(); i++)
        {
            try
            {
                String timeString = times.get(i);
                ZonedDateTime dateTime;

                // Handle OpenMeteo time format which might be with or without timezone
                if (timeString.contains("T") && !timeString.contains("Z") && !timeString.contains("+") && !timeString.substring(
                                timeString.indexOf("T")).contains("-"))
                {
                    // Format like "2025-11-05T15:00" - add UTC timezone
                    dateTime = ZonedDateTime.parse(timeString + "Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }
                else
                {
                    // Format with timezone information
                    dateTime = ZonedDateTime.parse(timeString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                }

                // Convert sunshine duration from seconds to minutes
                // OpenMeteo returns sunshine duration in seconds for the past hour
                Double sunshineSeconds = sunshineDurations.get(i);
                int sunshineMinutes = sunshineSeconds != null ? (int)Math.round(sunshineSeconds / 60.0) : 0;

                if (sunshineMinutes > 60)
                {
                    throw new IllegalArgumentException("Sunshine minutes exceed 60: " + sunshineMinutes);
                }

                result.put(dateTime, sunshineMinutes);

                logger.trace("Parsed {}: {} minutes sunshine", dateTime, sunshineMinutes);
            }
            catch(Exception e)
            {
                logger.warn("Failed to parse time entry at index {}: '{}', sunshine: {}", i, times.get(i), sunshineDurations.get(i), e);
            }
        }

        logger.info("Converted {} OpenMeteo data points to sunshine duration map", result.size());
        return result;
    }
}
