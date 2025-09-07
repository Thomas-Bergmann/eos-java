package de.hatoka.eos.devices.internal.business.forecast;

import de.hatoka.eos.devices.capi.business.config.CsvPriceConfig;
import de.hatoka.eos.devices.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.devices.capi.units.Money;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CsvStockEnergyPriceProvider implements EnergyPriceForecast
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");
    
    private final Map<String, Map<Integer, Double>> pricesByDateAndHour = new ConcurrentHashMap<>();
    private final CsvPriceConfig config;

    public CsvStockEnergyPriceProvider(CsvPriceConfig config)
    {
        this.config = config;
        loadResources();
    }

    private void loadResources()
    {
        for (String resource : config.resource())
        {
            loadCsvResource(resource);
        }
    }

    private void loadCsvResource(String resourcePath)
    {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath))
        {
            if (inputStream == null)
            {
                throw new IllegalArgumentException("Resource not found: " + resourcePath);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream)))
            {
                reader.lines()
                    .filter(line -> !line.trim().isEmpty() && !line.startsWith("#"))
                    .map(String::trim)
                    .forEach(this::parseCsvLine);
            }
        }
        catch (IOException e)
        {
            throw new RuntimeException("Failed to load CSV resource: " + resourcePath, e);
        }
    }

    private void parseCsvLine(String line)
    {
        String[] parts = line.split(",");
        if (parts.length < 2)
        {
            return; // Skip invalid lines
        }

        String dateStr = parts[0].trim();
        Map<Integer, Double> hourlyPrices = new HashMap<>();

        // Parse hourly prices (parts[1] = hour 0, parts[2] = hour 1, etc.)
        for (int hour = 0; hour < Math.min(24, parts.length - 1); hour++)
        {
            try
            {
                double price = Double.parseDouble(parts[hour + 1].trim());
                hourlyPrices.put(hour, price);
            }
            catch (NumberFormatException e)
            {
                // Skip invalid price values
            }
        }

        pricesByDateAndHour.put(dateStr, hourlyPrices);
    }

    @Override
    public Money getImportPrice(ZonedDateTime time)
    {
        Money marketPrice = getMarketPrice(time);
        if (marketPrice == null)
        {
            throw new IllegalStateException("No price available for time: " + time);
        }
        return marketPrice.add(config.importCharge().price());
    }

    @Override
    public Money getExportPrice(ZonedDateTime time)
    {
        Money marketPrice = getMarketPrice(time);
        if (marketPrice == null)
        {
            throw new IllegalStateException("No price available for time: " + time);
        }
        return marketPrice.subtract(config.exportCharge().price());
    }

    /**
     * @param time to lookup
     * @return price for kWh
     */
    private Money getMarketPrice(ZonedDateTime time)
    {
        String dateKey = time.format(DATE_FORMATTER);
        int hour = time.getHour();
        
        Map<Integer, Double> dayPrices = pricesByDateAndHour.get(dateKey);
        if (dayPrices == null)
        {
            return null;
        }
        Double dayPrice = dayPrices.get(hour);
        if (dayPrice == null)
        {
            return null;
        }
        return new Money(java.math.BigDecimal.valueOf(dayPrice), config.currency()).divide(1000);
    }
}
