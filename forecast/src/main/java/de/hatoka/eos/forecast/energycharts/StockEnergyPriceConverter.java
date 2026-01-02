package de.hatoka.eos.forecast.energycharts;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.StringWriter;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StockEnergyPriceConverter
{
    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");
    private static final String DATE_PATTERN = "<text[^>]*>\\s*(\\d{2}/\\d{2}/\\d{4})\\s*</text>";

    // Search directly for the path element with the long coordinate sequence
    // This is the red price line at the end of the SVG
    private static final String PATH_PATTERN = "<path[^>]*d=\"(M 3\\.055[^\"]+876\\.94444[^\"]+)\"[^>]*stroke=\"rgb\\(228, 26, 28\\)\"[^>]*>";
    private static final String COORDINATES_PATTERN = "([ML]?)\\s*([0-9.]+)\\s+([0-9.]+)";
    private static final String PATH_PRICE_PATTERN = "<path[^>]*d=\"([^\"]+)\"[^>]*stroke=\"rgb\\(228, 26, 28\\)\"[^>]*>";
    // Pattern to extract Y-axis price labels - handle multi-line formatting with whitespace
    private static final String Y_AXIS_PRICE_LABEL_PATTERN = "text-anchor=\"end\"[^>]*>\\s*(-?\\d+)\\s*<";
    private static final String RECT_PATTERN = "<rect[^>]*class=\"highcharts-plot-background\"[^>]*y=\"([0-9.]+)\"[^>]*height=\"([0-9.]+)\"[^>]*>";

    record PriceRange(double min, double max)
    {
    }

    private record ChartDimensions(double yOffset, double height)
    {
    }

    private record Coordinate(double x, double y)
    {
    }

    record DateExtractorResult(LocalDate startDate, int numberOfDays)
    {
    }

    public String exportToCSV(ZoneId zone, String... svgContents) throws IOException
    {
        Map<Instant, Double> prices = new HashMap<>();
        for(String svgContent : svgContents)
        {
            prices.putAll(extractStockPrices(svgContent));
        }
        Map<String, List<String>> priceLists = consolidatePricesPerDay(prices, zone);
        return exportToCSV(priceLists);
    }

    String exportToCSV(Map<String, List<String>> priceLists) throws IOException
    {
        // consolidate per day
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter(",").setSkipHeaderRecord(true).get();
        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat))
        {
            priceLists.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(entry -> {
                try
                {
                    List<Object> data = new ArrayList<>();
                    data.add(entry.getKey());
                    data.addAll(entry.getValue());
                    printer.printRecord(data);
                }
                catch(IOException e)
                {
                    throw new RuntimeException("Can't write csv data.", e);
                }
            });
        }
        return sw.toString().trim();
    }

    Map<String, List<String>> consolidatePricesPerDay(Map<Instant, Double> prices, ZoneId zone)
    {
        Map<String, Map<Integer, Double>> pricesPerDay = new HashMap<>();
        prices.forEach((time, price) -> {
            ZonedDateTime timeAtZone = time.atZone(zone);
            String lineDate = formatDate(timeAtZone);
            int hourOfDay = timeAtZone.getHour();
            pricesPerDay.computeIfAbsent(lineDate, l -> new HashMap<>()).put(hourOfDay, price);
        });
        Map<String, List<String>> result = new HashMap<>();
        pricesPerDay.forEach((day, pricesAtDay) -> {
            int maxNumber = pricesAtDay.keySet().stream().mapToInt(i -> i).max().orElse(0);
            List<String> priceList = new ArrayList<>(maxNumber + 1);
            for (int i = 0; i <= maxNumber; i++)
            {
                priceList.add(convertToString(pricesAtDay.get(i)));
            }
            result.put(day, priceList);
        });
        return result;
    }

    private String convertToString(Double value)
    {
        return String.format("%.2f", value);
    }

    String formatDate(ZonedDateTime timeAtZone)
    {
        DateTimeFormatter pattern = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        return pattern.format(timeAtZone);
    }

    /**
     * @param svgContent
     * @return map of date/time with related prices
     */
    Map<Instant, Double> extractStockPrices(String svgContent)
    {
        // Extract the price line path data
        String pathData = extractPricePath(svgContent);
        if (pathData == null)
        {
            return new HashMap<>();
        }

        // Extract date range from the chart
        DateExtractorResult dateExtractor = extractStartDate(svgContent);
        LocalDate startDate = dateExtractor.startDate();
        int numberOfDays = dateExtractor.numberOfDays();

        // Extract price range dynamically from Y-axis labels
        PriceRange priceRange = extractPriceRange(svgContent);

        // Extract chart dimensions dynamically from SVG
        ChartDimensions chartDimensions = extractChartDimensions(svgContent);

        // Parse all coordinates from the path data and extract both timestamps and prices
        return extractTimestampsAndPrices(pathData, startDate, priceRange, chartDimensions, numberOfDays);
    }

    PriceRange extractPriceRange(String svgContent)
    {
        Pattern pricePattern = Pattern.compile(Y_AXIS_PRICE_LABEL_PATTERN);
        Matcher matcher = pricePattern.matcher(svgContent);

        double minPrice = Double.MAX_VALUE;
        double maxPrice = Double.MIN_VALUE;

        while(matcher.find())
        {
            try
            {
                double price = Double.parseDouble(matcher.group(1));
                minPrice = Math.min(minPrice, price);
                maxPrice = Math.max(maxPrice, price);
            }
            catch(NumberFormatException e)
            {
                // Skip invalid price values
            }
        }

        // Fallback to default values if no prices found
        if (minPrice == Double.MAX_VALUE || maxPrice == Double.MIN_VALUE)
        {
            throw new IllegalStateException("Can't retrieve min " + minPrice + " or max " + maxPrice + " value");
        }
        return new PriceRange(minPrice, maxPrice);
    }

    private ChartDimensions extractChartDimensions(String svgContent)
    {
        // Look for the plot background rectangle which defines the chart area
        // Pattern: <rect ... class="highcharts-plot-background" x="65" y="43" width="880" height="426" ...>
        Matcher matcher = Pattern.compile(RECT_PATTERN).matcher(svgContent);

        if (matcher.find())
        {
            try
            {
                double yOffset = Double.parseDouble(matcher.group(1));
                double height = Double.parseDouble(matcher.group(2));
                return new ChartDimensions(yOffset, height);
            }
            catch(NumberFormatException e)
            {
                // Fall back to default values
            }
        }
        throw new IllegalStateException("Can't retrieve chart dimension");
    }

    private Map<Instant, Double> extractTimestampsAndPrices(String pathData, LocalDate startDate, PriceRange priceRange,
                    ChartDimensions chartDimensions, int numberOfDays)
    {
        // Extract all coordinate pairs from the path using regex
        Pattern coordPattern = Pattern.compile(COORDINATES_PATTERN);
        Matcher matcher = coordPattern.matcher(pathData);

        // First, collect all X coordinates to find min and max dynamically
        double minX = Double.MAX_VALUE;
        double maxX = Double.MIN_VALUE;

        // Store coordinates for second pass
        List<Coordinate> coordinates = new ArrayList<>();

        while(matcher.find())
        {
            try
            {
                double x = Double.parseDouble(matcher.group(2));
                double y = Double.parseDouble(matcher.group(3));

                coordinates.add(new Coordinate(x, y));
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
            }
            catch(NumberFormatException e)
            {
                LoggerFactory.getLogger(getClass()).warn("Could not parse coordinates: {}, {} ", matcher.group(2), matcher.group(3));
            }
        }

        // Now process coordinates with dynamically determined min/max X values and price range
        Map<Instant, Double> priceMap = new HashMap<>();
        for (Coordinate coordinate : coordinates)
        {
            // Calculate timestamp and price from coordinates
            Instant timestamp = convertXToTimestamp(coordinate.x(), startDate, minX, maxX, numberOfDays);
            double price = convertYToPrice(coordinate.y(), priceRange, chartDimensions);
            priceMap.put(timestamp, price);
        }

        return priceMap;
    }

    private String extractPricePath(String svg)
    {
        Matcher matcher = Pattern.compile(PATH_PATTERN).matcher(svg);
        if (matcher.find())
        {
            return matcher.group(1);
        }

        // Fallback: Search for any path with red stroke
        matcher = Pattern.compile(PATH_PRICE_PATTERN).matcher(svg);
        if (matcher.find())
        {
            return matcher.group(1);
        }
        return null;
    }

    DateExtractorResult extractStartDate(String svg)
    {
        // Look for all date patterns and find the first (earliest) one
        Matcher matcher = Pattern.compile(DATE_PATTERN).matcher(svg);

        LocalDate earliestDate = null;
        int numberOfDates = 0;
        while(matcher.find())
        {
            String dateStr = matcher.group(1);
            String[] parts = dateStr.split("/");
            if (parts.length == 3)
            {
                try
                {
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);
                    int year = Integer.parseInt(parts[2]);
                    LocalDate currentDate = LocalDate.of(year, month, day);
                    numberOfDates++;
                    if (earliestDate == null || currentDate.isBefore(earliestDate))
                    {
                        earliestDate = currentDate;
                    }
                }
                catch(NumberFormatException e)
                {
                    // Invalid date format, skip
                }
            }
        }

        return new DateExtractorResult(earliestDate, numberOfDates);
    }

    private double convertYToPrice(double y, PriceRange priceRange, ChartDimensions chartDimensions)
    {
        // Convert SVG Y coordinate to price using dynamic price range and chart dimensions
        // Note: The price line coordinates are already relative to the chart area due to SVG transform
        // so we don't need to subtract the yOffset
        double normalizedY = y / chartDimensions.height;
        // Invert Y axis (SVG Y increases downward, but price increases upward)
        normalizedY = 1.0 - normalizedY;
        return priceRange.min + normalizedY * (priceRange.max - priceRange.min);
    }

    private Instant convertXToTimestamp(double x, LocalDate startDate, double minX, double maxX, int numberOfDays)
    {
        // Convert SVG X coordinate to timestamp
        // Chart spans 6 days (week 33), X goes from minX to maxX
        // The X coordinates are now dynamically determined from the actual data

        // Calculate the normalized position within the chart
        // The first X coordinate should lead to 00:00 on the start date

        // Normalize X between 0 and 1
        double normalizedX = (x - minX) / (maxX - minX);

        // Chart shows 6 days * 24 hours = 144 hours (0-143)
        // We want the first point to be at hour 0 of day 1,
        // not at a negative hour
        int totalHours = numberOfDays * 24; // 144 hours for 6 days; 168 for 7 days
        double hoursFromStart = normalizedX * (totalHours - 1); // 0 to 143/167

        // Round to the next full hour for clean hourly data points
        long hourIndex = Math.round(hoursFromStart);

        // Ensure we stay within the expected hours
        if (hourIndex != Math.max(0, Math.min(totalHours - 1, hourIndex)))
        {
            throw new IllegalStateException("We calculated an hour outside of the range");
        }

        LocalDateTime startDateTime = startDate.atStartOfDay(); // start date 00:00
        LocalDateTime pointDateTime = startDateTime.plusHours(hourIndex);

        return pointDateTime.atZone(ZONE_ID).toInstant();
    }
}
