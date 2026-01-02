package de.hatoka.eos.forecast.energycharts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockDao;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockKey;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockPO;
import de.hatoka.eos.units.capi.Money;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.time.temporal.WeekFields;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Singleton
public class EnergyChartsImporter
{
    private static final String URI_FORMAT = "https://energy-charts.info/charts/price_spot_market/data/de/week_15min_%s_%02d.json"; // format parameters: year, week
    private static final String DAY_AHEAD_AUCTION_NAME_EN = "Day Ahead Auction (DE-LU)";
    private static final Logger logger = LoggerFactory.getLogger(EnergyChartsImporter.class);
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Inject
    private EnergyStockDao stockDao;

    public void importStockData(ZonedDateTime startDate)
                    throws IOException, InterruptedException
    {
        List<EnergyChartsResponse> responses = downloadData(URI.create(URI_FORMAT.formatted(getYearOfWeek(startDate), getWeekOfYear(startDate))));
        EnergyChartsResponse dayAheadAuction = findDayAheadAuction(responses);
        Map<ZonedDateTime, Double> dayAheadPrices = mapDataToTime(startDate, dayAheadAuction.getData());
        String currency = dayAheadAuction.getCurrency();
        Map<ZonedDateTime, Money> dayAheadPricesAsMoney = dayAheadPrices.entrySet().stream()
                .filter(e -> e.getValue() != null) // may some prices in the future are still not defined (null)
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> new Money(java.math.BigDecimal.valueOf(entry.getValue()), currency)
                ));
        storePrices(dayAheadPricesAsMoney);
    }

    private void storePrices(Map<ZonedDateTime, Money> dayAheadPrices)
    {
        for (Map.Entry<ZonedDateTime, Money> dayAheadPrice : dayAheadPrices.entrySet())
        {
            EnergyStockKey key = new EnergyStockKey(dayAheadPrice.getKey());
            EnergyStockPO existingData = stockDao.get(key);
            if (existingData == null)
            {
                existingData = new EnergyStockPO();
            }
            existingData.setDayAheadPrice(dayAheadPrice.getValue());
            stockDao.update(key, existingData);
        }
    }

    private Map<ZonedDateTime, Double> mapDataToTime(ZonedDateTime startDate, List<Double> priceData)
    {
        // Calculate the start of the week (Monday 00:00)
        ZonedDateTime dateOfPrice = getStartOfWeek(startDate);
        Map<ZonedDateTime, Double> result = new HashMap<>();

        // Map each 15-minute interval to its price
        for (Double price : priceData)
        {
            result.put(dateOfPrice, price);
            dateOfPrice = dateOfPrice.plusMinutes(15);
        }
        return result;
    }

    private Integer getYearOfWeek(ZonedDateTime date)
    {
        // Use ISO week-based year to handle weeks that span years correctly
        return date.get(WeekFields.ISO.weekBasedYear());
    }

    private ZonedDateTime getStartOfWeek(ZonedDateTime date)
    {
        // Get Monday of the week at 00:00
        return date.with(DayOfWeek.MONDAY)
                   .withHour(0)
                   .withMinute(0)
                   .withSecond(0)
                   .withNano(0);
    }

    private Integer getWeekOfYear(ZonedDateTime date)
    {
        // Use ISO week fields where Monday is the first day of the week (German standard)
        WeekFields weekFields = WeekFields.ISO;
        return date.get(weekFields.weekOfWeekBasedYear());
    }

    private List<EnergyChartsResponse> downloadData(URI dataUrl) throws IOException, InterruptedException
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

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200)
            {
                throw new IOException("HTTP request failed with status code: " + response.statusCode());
            }
            return objectMapper.readValue(
                            response.body(),
                            new TypeReference<>() {}
            );
        }
    }

    /**
     * Find the "Day Ahead Auction (DE-LU)" section
     * @param responses response from Url or resource
     * @return day ahead auction
     */
    static EnergyChartsResponse findDayAheadAuction(List<EnergyChartsResponse> responses)
    {
        for (EnergyChartsResponse response : responses)
        {
            if (response.getName() != null)
            {
                Object nameAttribute = response.getName();
                if (nameAttribute instanceof List<?> listAttribute)
                {
                    Object firstNameAttribute = listAttribute.getFirst();
                    if (firstNameAttribute instanceof Map<?,?> mapName)
                    {
                        for(Map.Entry<?, ?> entry : mapName.entrySet())
                        {
                            if (entry.getKey() instanceof String language && entry.getValue() instanceof String name)
                            {
                                if ("en".equals(language) && DAY_AHEAD_AUCTION_NAME_EN.equals(name))
                                {
                                    return response;
                                }
                            }
                        }
                    }
                }
            }
        }
        throw new RuntimeException("Can't find " + DAY_AHEAD_AUCTION_NAME_EN);
    }
}
