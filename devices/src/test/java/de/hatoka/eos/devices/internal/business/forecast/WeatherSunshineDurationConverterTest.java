package de.hatoka.eos.devices.internal.business.forecast;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class WeatherSunshineDurationConverterTest
{
    private static final String TEST_IMAGE = "weather_20251024.png";
    private static final LocalDate START_DATE = LocalDate.of(2025, 10, 24);
    private final WeatherSunshineDurationConverter converter = new WeatherSunshineDurationConverter();
    private final WeatherSunshineDurationConverter.SunHourRegion region = WeatherSunshineDurationConverter.WINDOW_DAY1;

    @Test
    void testCalculateHourXPosition()
    {
        // Hour 2: X = 183 + (2-2) * 6.75 = 183
        assertEquals(183, converter.calculateHourXPosition(region,2), "marker between 189 und 187");
        assertEquals(190, converter.calculateHourXPosition(region,3));
        assertEquals(231, converter.calculateHourXPosition(region,9));
        assertEquals(237, converter.calculateHourXPosition(region,10));

        // Test all hours 2-23
        System.out.println("\nAll hour positions:");
        for (int hour = 2; hour <= 23; hour++)
        {
            int x = converter.calculateHourXPosition(region, hour);
            System.out.printf("Hour %2d: X=%d%n", hour, x);
        }
    }

    @Test
    void testColorAtHourPositions() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        // position 407 is one pixel above the zero line
        assertEquals(WeatherSunshineDurationConverter.GRAY, new Color(image.getRGB(converter.calculateHourXPosition(region,2), 407)));
        assertEquals(WeatherSunshineDurationConverter.BLACK, new Color(image.getRGB(converter.calculateHourXPosition(region,9), 407)));
        assertEquals(WeatherSunshineDurationConverter.YELLOW, new Color(image.getRGB(converter.calculateHourXPosition(region,10), 407)));
    }

    @Test
    void testMeasureBarHeights() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        assertEquals(0, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,2), region));
        assertEquals(1, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,9), region));
        assertEquals(15, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,10), region));
        assertEquals(5, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,13), region));
    }

    @Test
    void testSunMinutes() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        assertEquals(0, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,2), region));
        assertEquals(2, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,9), region));
        assertEquals(26, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,10), region));
        assertEquals(26, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,11), region));
        assertEquals(4, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,12), region));
        assertEquals(9, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,13), region));
        assertEquals(11, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,14), region));
    }

    @Test
    void testExtractSunshineDuration() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        LocalDate nextDay = START_DATE.plusDays(1);

        Map<LocalDateTime, Integer> result = converter.extractSunshineDuration(image, START_DATE);

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(48, result.size());

        // Verify specific hourly values from testSunMinutes (hours 2-23 of day 1)
        assertEquals(0, result.get(LocalDateTime.of(START_DATE, LocalTime.of(2, 0))));
        assertEquals(2, result.get(LocalDateTime.of(START_DATE, LocalTime.of(9, 0))));
        assertEquals(26, result.get(LocalDateTime.of(START_DATE, LocalTime.of(10, 0))));
        assertEquals(26, result.get(LocalDateTime.of(START_DATE, LocalTime.of(11, 0))));
        assertEquals(4, result.get(LocalDateTime.of(START_DATE, LocalTime.of(12, 0))));
        assertEquals(9, result.get(LocalDateTime.of(START_DATE, LocalTime.of(13, 0))));
        assertEquals(11, result.get(LocalDateTime.of(START_DATE, LocalTime.of(14, 0))));

        // Verify hours 0 and 1 of next day are included
        assertTrue(result.containsKey(LocalDateTime.of(nextDay, LocalTime.of(0, 0))));
        assertTrue(result.containsKey(LocalDateTime.of(nextDay, LocalTime.of(1, 0))));
    }

    @Test
    void testExportToCSV() throws IOException
    {
        String csv = converter.exportToCSV(START_DATE, TEST_IMAGE);
        assertNotNull(csv);
        assertFalse(csv.contains("."));

        String[] lines = csv.split("\n");
        assertEquals(3, lines.length);

        // First line: date 10/24 with hours 2-23 (22 values)
        String[] values1 = lines[0].split(",");
        assertEquals(23, values1.length); // date + 22 hour values
        assertEquals("2025/10/25", values1[0]);

        // Verify specific values from testSunMinutes (hours 2,9,10,11,12,13,14 are at indices 1,8,9,10,11,12,13)
        assertEquals("0", values1[1]);  // hour 2
        assertEquals("2", values1[8]);  // hour 9
        assertEquals("26", values1[9]); // hour 10
        assertEquals("26", values1[10]); // hour 11
        assertEquals("4", values1[11]); // hour 12
        assertEquals("9", values1[12]); // hour 13
        assertEquals("11", values1[13]); // hour 14

        // Second line: date 10/25 full (24 hours)
        String[] values2 = lines[1].split(",");
        assertEquals(25, values2.length); // date + 24 hour values
        assertEquals("2025/10/25", values2[0]);

        // Third line: date 10/26 with hours 0-1 (2 values)
        String[] values3 = lines[2].split(",");
        assertEquals(3, values3.length); // date + 2 hour values
        assertEquals("2025/10/26", values3[0]);
    }

    @Test
    void testTotalSunshineHours() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        Map<LocalDateTime, Integer> result = converter.extractSunshineDuration(image, START_DATE);
        LocalDate day2 = START_DATE.plusDays(1);

        assertEquals(1.6 * 60, getTotalMinutes(result, START_DATE), 6);
        assertEquals(1.2 * 60, getTotalMinutes(result, day2), 6);
    }

    private static int getTotalMinutes(Map<LocalDateTime, Integer> result, LocalDate day)
    {
        return result.entrySet()
                                 .stream()
                                 .filter(e -> e.getKey().getDayOfMonth() == day.getDayOfMonth())
                                 .map(Map.Entry::getValue)
                                 .mapToInt(Integer::intValue)
                                 .sum();
    }

    @Test
    void testSecondDay() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE);
        LocalDate day2 = START_DATE.plusDays(1);
        LocalDate day3 = START_DATE.plusDays(2);

        Map<LocalDateTime, Integer> result = converter.extractSunshineDuration(image, START_DATE);
        assertEquals(48, result.size()); // startDate and 2 now
        assertTrue(result.containsKey(LocalDateTime.of(START_DATE, LocalTime.of(2, 0))), "day1 first hour");
        assertTrue(result.containsKey(LocalDateTime.of(day2, LocalTime.of(2, 0))), "day2 first hour");
        assertTrue(result.containsKey(LocalDateTime.of(day3, LocalTime.of(1, 0))), "day2 last hour on day3");

        // Verify specific hourly values from testSunMinutes (hours 2-23 of day 1)
        assertEquals(0, result.get(LocalDateTime.of(day2, LocalTime.of(2, 0))));
        assertEquals(9, result.get(LocalDateTime.of(day2, LocalTime.of(9, 0))));
        assertEquals(14, result.get(LocalDateTime.of(day2, LocalTime.of(10, 0))));
        assertEquals(18, result.get(LocalDateTime.of(day2, LocalTime.of(11, 0))));
        assertEquals(14, result.get(LocalDateTime.of(day2, LocalTime.of(12, 0))));
        assertEquals(14, result.get(LocalDateTime.of(day2, LocalTime.of(13, 0))));
        assertEquals(7, result.get(LocalDateTime.of(day2, LocalTime.of(14, 0))));
        assertEquals(0, result.get(LocalDateTime.of(day2, LocalTime.of(15, 0))));
    }
}
