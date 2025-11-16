package de.hatoka.eos.simulation.internal.business.forecast;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class StockEnergyPriceConverterTest
{
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    private final StockEnergyPriceConverter converter = new StockEnergyPriceConverter();
    private final Instant FIRST_DATE_WEEK33 = LocalDate.of(2025, 8, 11).atTime(LocalTime.MIDNIGHT).atZone(ZONE_ID).toInstant();
    private final Instant NEXT_DATE_WEEK33 = LocalDate.of(2025, 8, 12).atTime(LocalTime.MIDNIGHT).atZone(ZONE_ID).toInstant();
    private final Instant WEEK32_FIRST = LocalDate.of(2025, 8, 4).atTime(LocalTime.MIDNIGHT).atZone(ZONE_ID).toInstant();
    private final Instant WEEK32_SECOND = LocalDate.of(2025, 8, 5).atTime(LocalTime.MIDNIGHT).atZone(ZONE_ID).toInstant();
    private final Instant WEEK32_LAST = LocalDate.of(2025, 8, 10).atTime(LocalTime.of(23, 0)).atZone(ZONE_ID).toInstant();

    @Test
    void testStartDate() throws IOException
    {
        String svgContent = loadIncompleteWeek();
        var dateExtractorResult = converter.extractStartDate(svgContent);
        LocalDate actualDate = dateExtractorResult.startDate();
        LocalDate expectedDate = LocalDate.of(2025, 8, 11);
        assertEquals(expectedDate, actualDate);
        assertEquals(6, dateExtractorResult.numberOfDays(), "incomplete week");
    }

    @Test
    void testDateRangeAndHourlyDataPointsWeek33() throws IOException
    {
        // Read the test SVG file from classpath
        String svgContent = loadIncompleteWeek();

        Map<Instant, Double> prices = converter.extractStockPrices(svgContent);
        // Verify that we got data points
        assertFalse(prices.isEmpty(), "Should extract price data points");
        // Verify total points: 6 days × 24 hours = 144 points
        assertEquals(144, prices.size(), "Should have total of 144 data points (6 days × 24 hours)");
        // explicit dates (rough from diagram)
        assertEquals(99.82, prices.get(FIRST_DATE_WEEK33), 0.01, "check first entry");
        assertEquals(93.0, prices.get(NEXT_DATE_WEEK33), 0.01, "check first entry");
        // min and max (rough from diagram)
        assertEquals(-0.01, prices.values().stream().mapToDouble(Double::doubleValue).min().orElse(Double.MIN_VALUE), 0.01, "min should be around 0");
        assertEquals(283.89, prices.values().stream().mapToDouble(Double::doubleValue).max().orElse(Double.MIN_VALUE), 0.01, "max should be around 290");
    }

    @Test
    void testDateRangeAndHourlyDataPointsWeek32() throws IOException
    {
        // Read the test SVG file from classpath
        String svgContent = loadCompleteWeek();

        Map<Instant, Double> prices = converter.extractStockPrices(svgContent);
        // Verify that we got data points
        assertFalse(prices.isEmpty(), "Should extract price data points");
        // Verify total points: 7 days × 24 hours = 168 points
        assertEquals(168, prices.size(), "Should have total of 144 data points (6 days × 24 hours)");
        // explicit dates (rough from diagram)
        assertEquals(81.05, prices.get(WEEK32_FIRST), 0.01, "check first entry");
        assertEquals(21.52, prices.get(WEEK32_SECOND), 0.01, "check first entry");
        // min and max (rough from diagram)
        assertEquals(-61.08, prices.values().stream().mapToDouble(Double::doubleValue).min().orElse(Double.MIN_VALUE), 0.01, "min should be around 0");
        assertEquals(170.16, prices.values().stream().mapToDouble(Double::doubleValue).max().orElse(Double.MIN_VALUE), 0.01, "max should be around 290");
    }
    @Test
    void testWeek32Timestamps() throws IOException
    {
        // Read the test SVG file from classpath
        String svgContent = loadCompleteWeek();
        Map<Instant, Double> prices = converter.extractStockPrices(svgContent);
        List<Instant> times = prices.keySet().stream().sorted().toList();
        assertEquals(WEEK32_FIRST, times.getFirst());
        assertEquals(WEEK32_LAST, times.getLast());
        assertEquals(168, times.size(), "Should have total of 168 data points (7 days × 24 hours)");
    }

    @Test
    void testExtractStockPrices() throws IOException
    {
        // Read the test SVG file from classpath
        String svgContent = loadIncompleteWeek();

        StockEnergyPriceConverter converter = new StockEnergyPriceConverter();
        Map<Instant, Double> prices = converter.extractStockPrices(svgContent);

        // Verify that we got some data points
        assertFalse(prices.isEmpty(), "Should extract price data points");

        List<Map.Entry<Instant, Double>> firstDateEntries = prices.entrySet().stream().filter(e -> e.getKey().isBefore(NEXT_DATE_WEEK33)).toList();
        List<Double> pricesOfFirstDay = firstDateEntries.stream().map(Map.Entry::getValue).toList();
        // Verify price range is reasonable
        double minPrice = pricesOfFirstDay.stream().mapToDouble(Double::doubleValue).min().orElse(0);
        double maxPrice = pricesOfFirstDay.stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double avgPrice = pricesOfFirstDay.stream().mapToDouble(Double::doubleValue).average().orElse(0);

        assertEquals(0.0, minPrice, 0.001, "Min price should be reasonable: " + minPrice);
        assertEquals(191.95, maxPrice, 0.001, "Max price should be reasonable: " + maxPrice);
        assertEquals(24, pricesOfFirstDay.size(), "Should have many data points, got: " + pricesOfFirstDay.size());
    }

    @Test
    void testExtractPriceRange() throws IOException
    {
        // Read the test SVG file from classpath
        String svgContent = loadIncompleteWeek();
        StockEnergyPriceConverter.PriceRange priceRange = converter.extractPriceRange(svgContent);

        // Verify the expected price range
        assertEquals(-60.0, priceRange.min(), 0.01, "Min price should be -60 EUR/MWh");
        assertEquals(300.0, priceRange.max(), 0.01, "Max price should be 300 EUR/MWh");
    }

    @Test
    void testExportCSVContent() throws IOException
    {
        String svgContent = loadIncompleteWeek();
        Map<Instant, Double> prices = converter.extractStockPrices(svgContent);
        Map<String, List<String>> pricesPerDay = converter.consolidatePricesPerDay(prices, ZONE_ID);
        assertEquals(6, pricesPerDay.size());
        List<String> pricesOfFirstDay = pricesPerDay.get(converter.formatDate(FIRST_DATE_WEEK33.atZone(ZONE_ID)));
        assertNotNull(pricesOfFirstDay);
        assertEquals(24, pricesOfFirstDay.size());
        String content = converter.exportToCSV(pricesPerDay);
        assertTrue(content.contains("2025/08/11,99.82,"), content);
    }

    @Test
    // @Disabled
    void testExportCSVFiles() throws IOException
    {
        String[] resources = Stream.of("stockprices_DE_LU_week_32_2025.svg", "stockprices_DE_LU_week_33_2025.svg")
                                   .map(this::loadResource).toArray(String[]::new);
        String content = converter.exportToCSV(ZONE_ID, resources);
        Path filePath = Files.createTempFile("stockprices_DE_LU", ".csv");
        System.out.printf("File: %s", filePath.toString());
        Files.writeString(filePath, content);
    }

    private String loadIncompleteWeek() throws IOException
    {
        return loadResource("stockprices_DE_LU_week_33_2025.svg");
    }
    private String loadCompleteWeek() throws IOException
    {
        return loadResource("stockprices_DE_LU_week_32_2025.svg");
    }

    private String loadResource(String resource)
    {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource))
        {
            if (is == null)
            {
                throw new IOException("Resource not found: '" + resource + "'");
            }
            return new String(is.readAllBytes(), StandardCharsets.UTF_8);
        }
        catch(IOException e)
        {
            throw new RuntimeException("Can't load resource", e);
        }
    }
}
