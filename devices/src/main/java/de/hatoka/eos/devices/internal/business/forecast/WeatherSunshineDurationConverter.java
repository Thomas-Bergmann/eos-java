package de.hatoka.eos.devices.internal.business.forecast;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;

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
public class WeatherSunshineDurationConverter
{
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    static final Color YELLOW = new Color(249, 220, 32);
    static final Color GRAY = new Color(220, 220, 220);
    static final Color BLACK = Color.BLACK;
    static final SunHourRegion WINDOW_DAY1 = new SunHourRegion(180, 342, 373, 408);

    // Bar positioning constants (from Excel analysis)
    private static final double START_X = 183; // Middle between 2h and 3h markers
    private static final double PIXELS_PER_HOUR = 6.75;
    private static final int DAY_START_HOUR = 2; // Days start at 2h, not 0h

    /**
     * Calculates the X coordinate for a given hour in the weather image.
     *
     * Formula: X = 183 + (hour - 2) * 6.75
     * where hour is 0-23 (0h, 1h, ..., 23h)
     *
     * Examples:
     * - Hour 2: X = 183 + (2-2) * 6.75 = 183
     * - Hour 9: X = 183 + (9-2) * 6.75 = 183 + 47.25 = 230.25 → 230
     * - Hour 10: X = 183 + (10-2) * 6.75 = 183 + 54 = 237
     *
     * @param hour the hour (0-23)
     * @return the X coordinate in pixels
     */
    int calculateHourXPosition(int hour)
    {
        if (hour < DAY_START_HOUR)
        {
            throw new IllegalArgumentException("Hour " + hour + " is before day start at " + DAY_START_HOUR + "h");
        }
        double x = START_X + (hour - DAY_START_HOUR) * PIXELS_PER_HOUR;
        return (int) Math.round(x);
    }

    /**
     * Exports sunshine duration data from PNG image to CSV format.
     *
     * @param startDate the start date of the forecast
     * @param pngResourceName the name of the PNG resource file
     * @return CSV string with format: date,hour0,hour1,...,hour23
     * @throws IOException if reading the image fails
     */
    public String exportToCSV(LocalDate startDate, String pngResourceName) throws IOException
    {
        BufferedImage image = loadImage(pngResourceName);
        Map<LocalDate, List<Integer>> sunshineDurationPerDay = extractSunshineDuration(image, startDate);
        return exportToCSV(sunshineDurationPerDay);
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
     * Extracts sunshine duration data from the weather forecast image.
     * The image contains hourly sunshine duration bars in yellow color.
     *
     * @param image the weather forecast image
     * @param startDate the start date of the forecast
     * @return map of dates to hourly sunshine duration values in minutes (0-60)
     */
    Map<LocalDate, List<Integer>> extractSunshineDuration(BufferedImage image, LocalDate startDate)
    {
        int width = image.getWidth();
        int height = image.getHeight();

        LoggerFactory.getLogger(getClass()).info("Extracting sunshine duration from image: {}x{}", width, height);

        // Find the actual yellow bar region
        // first day
        LoggerFactory.getLogger(getClass()).info("Yellow region found: Y={} to {}, X={} to {}",
                        WINDOW_DAY1.minY, WINDOW_DAY1.maxY, WINDOW_DAY1.minX, WINDOW_DAY1.maxX);

        // Extract hourly values from the yellow bars using the markers
        List<Integer> hourlyValues = extractHourlyValues(image, WINDOW_DAY1);

        // Group hourly values by day
        // Note: Days start at 2h, not 0h! First day has 22 hours (2h-23h), subsequent days have 24 hours
        Map<LocalDate, List<Integer>> result = new HashMap<>();

        if (hourlyValues.isEmpty())
        {
            return result;
        }

        // First day: starts at 2h, so we need to prepend two 0 values for hours 0 and 1
        List<Integer> firstDayValues = new ArrayList<>();
        firstDayValues.add(0); // Hour 0
        firstDayValues.add(0); // Hour 1
        int valuesInFirstDay = Math.min(22, hourlyValues.size()); // 2h-23h = 22 hours
        firstDayValues.addAll(hourlyValues.subList(0, valuesInFirstDay));
        result.put(startDate, firstDayValues);

        // Subsequent full days (24 hours each)
        int index = 22; // Start after first day's 22 hours
        int dayOffset = 1;
        while (index < hourlyValues.size())
        {
            LocalDate date = startDate.plusDays(dayOffset);
            int endIndex = Math.min(index + 24, hourlyValues.size());
            List<Integer> dayValues = new ArrayList<>(hourlyValues.subList(index, endIndex));
            result.put(date, dayValues);

            index += 24;
            dayOffset++;
        }

        return result;
    }

    private record SunHourRegion(int minX, int maxX, int minY, int maxY) {}

    /**
     * Extracts hourly sunshine duration values from bars.
     * The bar height represents the sunshine duration for that hour.
     *
     * Strategy: Based on Excel analysis:
     * - Average bar width: 6.75 pixels per hour
     * - Start at X=183 (middle between 2h and 3h markers)
     * - For hour N (starting at 2h): X = 183 + (N-2) * 6.75
     * - Bar height measured from Y=407 (bottom) up to colored pixels
     *
     * @param image the weather forecast image
     * @param region the region containing the sunshine bars
     * @return list of sunshine minutes (0-60) for each hour
     */
    private List<Integer> extractHourlyValues(BufferedImage image, SunHourRegion region)
    {
        List<Integer> values = new ArrayList<>();

        // Extract values for hours 2-23 (day starts at 2h)
        // Determine the last hour we can extract based on image width
        int maxHour = 23; // Try up to 23h
        while (maxHour > DAY_START_HOUR)
        {
            int x = calculateHourXPosition(maxHour);
            if (x < region.maxX)
            {
                break; // This hour is within bounds
            }
            maxHour--;
        }

        LoggerFactory.getLogger(getClass()).info("Extracting hours {} to {}", DAY_START_HOUR, maxHour);

        for (int hour = DAY_START_HOUR; hour <= maxHour; hour++)
        {
            // Get X position for this hour
            int hourX = calculateHourXPosition(hour);

            if (hourX >= image.getWidth())
            {
                break; // Reached image boundary
            }

            // Calculate sunshine minutes for this hour using the refactored calcSunMinutes method
            int sunMinutes = calcSunMinutes(image, hourX, region);
            values.add(sunMinutes);
        }

        LoggerFactory.getLogger(getClass()).info("Extracted {} hourly values", values.size());
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
     * Checks if a pixel color is in the yellow range (sunshine bar color).
     */
    private boolean isYellow(int rgb)
    {
        return isYellowPixel(new Color(rgb));
    }

    String exportToCSV(Map<LocalDate, List<Integer>> sunshineDurationPerDay) throws IOException
    {
        StringWriter sw = new StringWriter();
        CSVFormat csvFormat = CSVFormat.DEFAULT.builder().setDelimiter(",").setSkipHeaderRecord(true).build();

        try (final CSVPrinter printer = new CSVPrinter(sw, csvFormat))
        {
            sunshineDurationPerDay.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(entry -> {
                    try
                    {
                        List<Object> data = new ArrayList<>();
                        data.add(formatDate(entry.getKey()));
                        data.addAll(entry.getValue().stream()
                            .map(this::formatInteger)
                            .toList());
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

    private String formatDate(LocalDate date)
    {
        return DATE_FORMATTER.format(date);
    }

    private String formatInteger(Integer value)
    {
        return value.toString();
    }
}
