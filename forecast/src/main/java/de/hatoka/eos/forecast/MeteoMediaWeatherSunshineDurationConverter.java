package de.hatoka.eos.forecast;

import jakarta.inject.Singleton;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Converts weather forecast PNG images to CSV format by extracting sunshine duration data.
 * The converter analyzes yellow bars in the PNG to determine sunshine hours per day.
 *
 * Image structure (from top to bottom):
 * 1. Temperature (12-hour max, 12-hour min)
 * 2. Wind gusts / Wind speed
 * 3. Wind direction
 * 4. Relative humidity
 * 5. Sunshine duration per day (yellow area)
 * 6. Sunshine duration per hour (yellow bars) - THIS IS WHAT WE EXTRACT
 * 7. Precipitation 6h
 */
@Singleton
public class MeteoMediaWeatherSunshineDurationConverter
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    static final Color YELLOW = new Color(249, 220, 32);
    static final Color GRAY = new Color(220, 220, 220);
    static final Color BLACK = Color.BLACK;

    // Extended window to include full 24-hour forecast day (hour 2 of day 1 through hour 1 of day 2)
    record SunHourRegion(int minX, int maxX, int minY, int maxY) {
        SunHourRegion nextDay()
        {
            return new SunHourRegion(maxX,maxX + (maxX - minX), minY, maxY);
        }
    }
    // picked up region from png image
    static final SunHourRegion WINDOW_DAY1 = new SunHourRegion(180, 342, 373, 408);

    // Bar positioning constants (from Excel analysis)
    private static final double PIXELS_PER_HOUR =  ((double)(WINDOW_DAY1.maxX - WINDOW_DAY1.minX)) / 24;

    /**
     * Calculates the X coordinate for a given index in the weather image.
     *
     * Formula: X = 183 + (index - 2) * 6.75
     * where index is 0-23 (0h, 1h, ..., 23h)
     *
     * Examples:
     * - Hour 2: X = 183 + (2-2) * 6.75 = 183
     * - Hour 9: X = 183 + (9-2) * 6.75 = 183 + 47.25 = 230.25 → 230
     * - Hour 10: X = 183 + (10-2) * 6.75 = 183 + 54 = 237
     *
     * @param index the index (0-23) of UTC
     * @return the X coordinate in pixels
     */
    int calculateHourXPosition(SunHourRegion region, int index)
    {
        double x = region.minX + PIXELS_PER_HOUR/2 + index * PIXELS_PER_HOUR;
        return (int) Math.round(x);
    }

    /**
     * Exports sunshine duration data from PNG image to CSV format.
     * Each row represents a date with hourly sunshine values as columns.
     *
     * @param startDate the start date of the image
     * @param pngResourceName the name of the PNG resource file
     * @return CSV string with format: date,hour_values...
     *         Example: 2025/10/24,0,0,2,26,26,4,9,11,... (date + values for hours present in that date)
     * @throws IOException if reading the image fails
     */
    public String exportToCSV(ZonedDateTime startDate, String pngResourceName) throws IOException
    {
        BufferedImage image = loadImage(pngResourceName);
        Map<ZonedDateTime, Integer> sunshineDurationPerHour = extractSunshineDuration(image, startDate);
        return exportToCSV(sunshineDurationPerHour, startDate.getZone());
    }

    BufferedImage loadImage(String resourceName) throws IOException
    {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resourceName))
        {
            if (is == null)
            {
                throw new IOException("Resource not found: " + resourceName);
            }
            return ImageIO.read(is);
        }
    }

    /**
     * Loads an image from a file path.
     *
     * @param file the image file
     * @return the loaded BufferedImage
     * @throws IOException if the file cannot be read
     */
    BufferedImage loadImageFromFile(File file) throws IOException
    {
        return ImageIO.read(file);
    }

    /**
     * Extracts sunshine duration data from the weather forecast image.
     * The image contains hourly sunshine duration bars in yellow color.
     * Each forecast "day" runs from hour 2 of one day through hour 1 of the next day (24 hours).
     *
     * @param image the weather forecast image
     * @param startDate the start date of the forecast (the date when hour 2 begins)
     * @return map of datetime (hour precision in UTC) to sunshine minutes for that hour
     */
    Map<ZonedDateTime, Integer> extractSunshineDuration(BufferedImage image, ZonedDateTime startDate)
    {
        Map<ZonedDateTime, Integer> result = new HashMap<>();
        result.putAll(extractSunshineDuration(image, startDate, WINDOW_DAY1));
        result.putAll(extractSunshineDuration(image, startDate.plusDays(1), WINDOW_DAY1.nextDay()));
        return result;
    }

    /**
     * Extracts sunshine duration data from the weather forecast image using a specific region.
     * The image contains hourly sunshine duration bars in yellow color.
     * Each forecast "day" runs from hour 2 of one day through hour 1 of the next day (24 hours).
     *
     * @param image the weather forecast image
     * @param startDate the start date of the forecast (the date when hour 2 begins)
     * @param region the region to extract from
     * @return map of datetime (hour precision in UTC) to sunshine minutes for that hour
     */
    Map<ZonedDateTime, Integer> extractSunshineDuration(BufferedImage image, ZonedDateTime startDate, SunHourRegion region)
    {
        // Find the actual yellow bar region
        LoggerFactory.getLogger(getClass()).debug("Find sun hours in region: Y={} to {}, X={} to {}", region.minY, region.maxY, region.minX, region.maxX);

        // Extract hourly values from the yellow bars using the markers
        // Returns 24 values: hours 2-23 of startDate, then hours 0-1 of startDate+1
        List<Integer> hourlyValues = extractHourlyValues(image, region);
        LoggerFactory.getLogger(getClass()).info("Day {} hourly values {}.", startDate, hourlyValues);

        // Map hourly values to their datetime
        // Forecast day: hour 2 of startDate through hour 1 of startDate+1
        Map<ZonedDateTime, Integer> result = new HashMap<>();
        if (hourlyValues.isEmpty())
        {
            return result;
        }

        // Map each hourly value to its datetime (automatically wraps to next day at midnight)
        ZonedDateTime currentDateTime = startDate;
        for (Integer sunMinutes : hourlyValues)
        {
            result.put(currentDateTime, sunMinutes);
            currentDateTime = currentDateTime.plusHours(1);
        }

        return result;
    }

    /**
     * Extracts hourly sunshine duration values from bars.
     * The bar height represents the sunshine duration for that hour.
     *
     * Strategy: Based on Excel analysis:
     * - Average bar width: 6.75 pixels per hour
     * - Start at X=183 (middle between 2h and 3h markers)
     * - For hour N (starting at 2h): X = 183 + (N-2) * 6.75
     * - Bar height measured from Y=407 (bottom) up to colored pixels
     * - Extracts full 24-hour forecast day: hour 2 of day 1 through hour 1 of day 2
     *
     * @param image the weather forecast image
     * @param region the region containing the sunshine bars
     * @return list of sunshine minutes (0-60) for each hour in sequence
     */
    private List<Integer> extractHourlyValues(BufferedImage image, SunHourRegion region)
    {
        List<Integer> values = new ArrayList<>();

        for (int index = 0; index < 24; index++)
        {
            // Get X position for this linear hour position
            int hourX = calculateHourXPosition(region, index);

            if (hourX >= image.getWidth() || hourX >= region.maxX)
            {
                break; // Reached image or region boundary
            }

            // Calculate sunshine minutes for this hour using the refactored calcSunMinutes method
            int sunMinutes = calcSunMinutes(image, hourX, region);
            values.add(sunMinutes);
        }
        return values;
    }

    int calcSunMinutes(BufferedImage image, int x, SunHourRegion region)
    {
        int pixel = measureBarHeightToBlackLine(image, x, region);
        int pixel100Percent = region.maxY - region.minY;
        return Math.ceilDiv(pixel * 60, pixel100Percent);
    }

    /**
     * Measures the bar height starting from Y=407 (bottom of bar area).
     *
     * Strategy:
     * 1. Check pixel at Y=407
     *    - If gray → 0% (no bar)
     *    - If black → very small value (bar < 1 pixel visible)
     *    - If yellow → scan upward until black line found
     *
     * @param image the image
     * @param x the X coordinate to measure at
     * @return height in pixels (0 = no bar/no sun )
     */
    int measureBarHeightToBlackLine(BufferedImage image, int x, SunHourRegion region)
    {
        int y = region.maxY - 1; // Bottom of bar area 408 is axe line
        int topLimit = region.minY; // Top boundary (where black line is)

        // Check the pixel at Y=407
        Color color = new Color(image.getRGB(x, y));
        if (isGrayPixel(color))
        {
            return 0;
        }
        while(isYellowPixel(color) && topLimit < y)
        {
            y--;
            color = new Color(image.getRGB(x, y));
        }
        return region.maxY - y;
    }

    private boolean isGrayPixel(Color color)
    {
        return GRAY.equals(color);
    }

    private boolean isYellowPixel(Color color)
    {
        return YELLOW.equals(color);
    }

    /**
     * Converts the given map(datetime, number) to a csv string (comma separated). Each line a new date at given timezone.
     * @param sunshineDurationPerHour time-based numbers
     * @param timeZone timezone to group times by date
     * @return comma csv file first column date followed by hours 0-23
     * @throws IOException if the string be written
     */
    String exportToCSV(Map<ZonedDateTime, Integer> sunshineDurationPerHour, ZoneId timeZone) throws IOException
    {
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter(",").setSkipHeaderRecord(true).get();

        // Group by date
        Map<LocalDate, List<Integer>> byDate = new java.util.TreeMap<>();
        sunshineDurationPerHour.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    LocalDate date = entry.getKey().withZoneSameInstant(timeZone).toLocalDate();
                    byDate.computeIfAbsent(date, k -> new ArrayList<>()).add(entry.getValue());
                });

        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat))
        {
            for (Map.Entry<LocalDate, List<Integer>> entry : byDate.entrySet())
            {
                List<Object> row = new ArrayList<>();
                row.add(DATE_FORMATTER.format(entry.getKey()));
                row.addAll(entry.getValue());
                printer.printRecord(row);
            }
        }

        return sw.toString().trim();
    }
}
