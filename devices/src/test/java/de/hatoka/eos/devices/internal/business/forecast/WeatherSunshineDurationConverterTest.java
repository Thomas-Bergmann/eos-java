package de.hatoka.eos.devices.internal.business.forecast;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WeatherSunshineDurationConverterTest
{
    private static final String TEST_IMAGE = "weather_20251024.png";

    private final WeatherSunshineDurationConverter converter = new WeatherSunshineDurationConverter();

    @Test
    void testCalculateHourXPosition()
    {
        // Hour 2: X = 183 + (2-2) * 6.75 = 183
        assertEquals(183, converter.calculateHourXPosition(2), "marker between 189 und 187");
        assertEquals(190, converter.calculateHourXPosition(3));
        assertEquals(230, converter.calculateHourXPosition(9));
        assertEquals(237, converter.calculateHourXPosition(10));

        // Test all hours 2-23
        System.out.println("\nAll hour positions:");
        for (int hour = 2; hour <= 23; hour++)
        {
            int x = converter.calculateHourXPosition(hour);
            System.out.printf("Hour %2d: X=%d%n", hour, x);
        }
    }

    @Test
    void testColorAtHourPositions() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        // position 407 is one pixel above the zero line
        assertEquals(WeatherSunshineDurationConverter.GRAY, new Color(image.getRGB(converter.calculateHourXPosition(2), 407)));
        assertEquals(WeatherSunshineDurationConverter.BLACK, new Color(image.getRGB(converter.calculateHourXPosition(9), 407)));
        assertEquals(WeatherSunshineDurationConverter.YELLOW, new Color(image.getRGB(converter.calculateHourXPosition(10), 407)));
    }

    @Test
    void testMeasureBarHeights() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);

        assertEquals(0, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(2), converter.WINDOW_DAY1));
        assertEquals(1, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(9), converter.WINDOW_DAY1));
        assertEquals(15, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(10), converter.WINDOW_DAY1));
        assertEquals(5, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(13), converter.WINDOW_DAY1));
    }

    @Test
    void testSunMinutes() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        assertEquals(0, converter.calcSunMinutes(image, converter.calculateHourXPosition(2), converter.WINDOW_DAY1));
        assertEquals(2, converter.calcSunMinutes(image, converter.calculateHourXPosition(9), converter.WINDOW_DAY1));
        assertEquals(26, converter.calcSunMinutes(image, converter.calculateHourXPosition(10), converter.WINDOW_DAY1));
        assertEquals(26, converter.calcSunMinutes(image, converter.calculateHourXPosition(11), converter.WINDOW_DAY1));
        assertEquals(4, converter.calcSunMinutes(image, converter.calculateHourXPosition(12), converter.WINDOW_DAY1));
        assertEquals(9, converter.calcSunMinutes(image, converter.calculateHourXPosition(13), converter.WINDOW_DAY1));
        assertEquals(11, converter.calcSunMinutes(image, converter.calculateHourXPosition(14), converter.WINDOW_DAY1));
    }

    @Test
    void testExtractSunshineDuration() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        LocalDate startDate = LocalDate.of(2025, 10, 24);

        Map<LocalDate, List<Integer>> result = converter.extractSunshineDuration(image, startDate);

        // Verify result contains data for the start date
        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertTrue(result.containsKey(startDate));

        // Verify first day has 24 values (hours 0-23)
        List<Integer> firstDay = result.get(startDate);
        assertEquals(24, firstDay.size(), "First day should have 24 hourly values");

        // Verify hours 0 and 1 are 0 (prepended values)
        assertEquals(0, firstDay.get(0), "Hour 0 should be 0");
        assertEquals(0, firstDay.get(1), "Hour 1 should be 0");

        // Verify all values are in valid range (0-60 minutes)
        for (int hour = 0; hour < firstDay.size(); hour++)
        {
            Integer minutes = firstDay.get(hour);
            assertNotNull(minutes, "Hour " + hour + " should not be null");
            assertTrue(minutes >= 0 && minutes <= 60,
                "Hour " + hour + " should be in range 0-60, but was: " + minutes);
        }

        // Verify known values from the test
        assertEquals(0, firstDay.get(2), "Hour 2 should have 0 sun minutes");
        assertEquals(2, firstDay.get(9), "Hour 9 should have 2 sun minutes");
        assertEquals(26, firstDay.get(10), "Hour 10 should have 26 sun minutes");
        assertEquals(26, firstDay.get(11), "Hour 11 should have 26 sun minutes");
    }

    @Test
    void testExportToCSV() throws IOException
    {
        LocalDate startDate = LocalDate.of(2025, 10, 24);

        String csv = converter.exportToCSV(startDate, TEST_IMAGE);

        assertNotNull(csv);
        assertFalse(csv.isEmpty());

        // Verify CSV format: date,hour0,hour1,...,hour23
        String[] lines = csv.split("\n");
        assertTrue(lines.length >= 1, "CSV should have at least one line");

        // Verify first line
        String firstLine = lines[0];
        assertTrue(firstLine.startsWith("2025/10/24,"), "First line should start with date");

        // Verify it contains integer values (not decimal)
        String[] values = firstLine.split(",");
        assertEquals(25, values.length, "Should have date + 24 hour values");

        // Verify date format
        assertEquals("2025/10/24", values[0]);

        // Verify hour values are integers (no decimal points)
        for (int i = 1; i < values.length; i++)
        {
            String value = values[i];
            assertFalse(value.contains("."), "Value should be integer, not decimal: " + value);
            int intValue = Integer.parseInt(value);
            assertTrue(intValue >= 0 && intValue <= 60,
                "Hour value should be in range 0-60: " + intValue);
        }

        System.out.println("CSV Output:");
        System.out.println(csv);
    }

    @Test
    void testAllHoursHaveValidValues() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        LocalDate startDate = LocalDate.of(2025, 10, 24);

        Map<LocalDate, List<Integer>> result = converter.extractSunshineDuration(image, startDate);
        List<Integer> firstDay = result.get(startDate);

        System.out.println("\nSunshine minutes for each hour on " + startDate + ":");
        for (int hour = 0; hour < firstDay.size(); hour++)
        {
            Integer minutes = firstDay.get(hour);
            System.out.printf("Hour %2d: %2d minutes%n", hour, minutes);
        }

        // Verify that we have some sunshine hours (not all zeros)
        long sunshineHours = firstDay.stream().filter(m -> m > 0).count();
        assertTrue(sunshineHours > 0, "Should have at least some sunshine hours");
    }
}
