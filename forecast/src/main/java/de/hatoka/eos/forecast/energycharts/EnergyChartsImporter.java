package de.hatoka.eos.forecast.energycharts;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class EnergyChartsImporter
{
    private static final String URI_FORMAT = "https://energy-charts.info/charts/price_spot_market/data/de/week_15min_%s_%s.json"; // day format parameter week, year
    private static final Logger logger = LoggerFactory.getLogger(EnergyChartsImporter.class);

    protected Map<ZonedDateTime, Double> downloadAndProcessWeatherData(ZonedDateTime startDate)
                    throws IOException, InterruptedException
    {
        Double[] imageData = downloadData(URI.create(URI_FORMAT.formatted(startDate.getYear(), getWeekOfYear(startDate))));
        // for each 15min while imageData is available
        ZonedDateTime dateOfPrice = getStartOfWeek(startDate);
        Map<ZonedDateTime, Double> result = new HashMap<>();
        for(Double price : imageData)
        {
            result.put(dateOfPrice, price);
            dateOfPrice = dateOfPrice.plusMinutes(15);
        }
        return result;
    }

    private ZonedDateTime getStartOfWeek(ZonedDateTime startDate)
    {
        // 2025/11/10 for 2025/11/16
        return startDate;
    }

    private Integer getWeekOfYear(ZonedDateTime startDate)
    {
        return 46;
    }

    private Double[] downloadData(URI dataUrl) throws IOException, InterruptedException
    {
        try (HttpClient httpClient = HttpClient.newBuilder()
                                               .connectTimeout(Duration.ofSeconds(30))
                                               .followRedirects(HttpClient.Redirect.NORMAL)
                                               .build())
        {
            HttpRequest request = HttpRequest.newBuilder()
                                             .uri(dataUrl)
                                             .timeout(Duration.ofSeconds(30))
                                             .header("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36")
                                             .header("Accept", "application/json")
                                             .GET()
                                             .build();

            logger.debug("Sending HTTP request to: {}", dataUrl);

            HttpResponse<byte[]> response = httpClient.send(request, HttpResponse.BodyHandlers.ofByteArray());

            if (response.statusCode() != 200)
            {
                throw new IOException("HTTP request failed with status code: " + response.statusCode());
            }
            return response.body();
        }
    }
}
