package de.hatoka.eos.forecast.meteomedia;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.*;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
class MeteoMediaWeatherSunshineDurationConverterTest
{
    private static final String TEST_IMAGE_CEST = "weather_20251024.png";
    private static final String TEST_IMAGE_CET = "weather_20251027.png";
    private static final LocalDate START_DATE_CEST = LocalDate.of(2025, 10, 24);
    private static final LocalDate START_DATE_CET = LocalDate.of(2025, 10, 27);
    private static final ZoneId BERLIN = ZoneId.of("Europe/Berlin");
    private static final ZoneId UTC = ZoneId.of("UTC");
    private final MeteoMediaWeatherSunshineDurationConverter converter = new MeteoMediaWeatherSunshineDurationConverter();
    private final MeteoMediaWeatherSunshineDurationConverter.SunHourRegion region = MeteoMediaWeatherSunshineDurationConverter.WINDOW_DAY1;

    @Test
    void testCalculateHourXPosition()
    {
        // Test all hours 2-23
        System.out.println("\nAll hour positions:");
        for (int position = 0; position < 24; position++)
        {
            int x = converter.calculateHourXPosition(region, position);
            System.out.printf("Hour(UTC) %2d: X=%d%n", position, x);
        }
        Assertions.assertEquals(183, converter.calculateHourXPosition(region,0), "marker between 180 und 187");
        Assertions.assertEquals(190, converter.calculateHourXPosition(region,1));
        Assertions.assertEquals(231, converter.calculateHourXPosition(region,7));
        Assertions.assertEquals(237, converter.calculateHourXPosition(region,8));

    }

    @Test
    void testColorAtHourPositions() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE_CEST);
        // position 407 is one pixel above the zero line
        Assertions.assertEquals(
                        MeteoMediaWeatherSunshineDurationConverter.GRAY, new Color(image.getRGB(converter.calculateHourXPosition(region,0), 407)));
        Assertions.assertEquals(
                        MeteoMediaWeatherSunshineDurationConverter.BLACK, new Color(image.getRGB(converter.calculateHourXPosition(region,7), 407)));
        Assertions.assertEquals(
                        MeteoMediaWeatherSunshineDurationConverter.YELLOW, new Color(image.getRGB(converter.calculateHourXPosition(region,8), 407)));
    }

    @Test
    void testMeasureBarHeights() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE_CEST);
        Assertions.assertEquals(0, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,0), region));
        Assertions.assertEquals(1, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,7), region));
        Assertions.assertEquals(15, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,8), region));
        Assertions.assertEquals(5, converter.measureBarHeightToBlackLine(image, converter.calculateHourXPosition(region,11), region));
    }

    @Test
    void testSunMinutes() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE_CEST);
        Assertions.assertEquals(0, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,0), region));
        Assertions.assertEquals(2, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,7), region));
        Assertions.assertEquals(26, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,8), region));
        Assertions.assertEquals(26, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,9), region));
        Assertions.assertEquals(4, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,10), region));
        Assertions.assertEquals(9, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,11), region));
        Assertions.assertEquals(11, converter.calcSunMinutes(image, converter.calculateHourXPosition(region,12), region));
    }

    @Test
    void testExtractSunshineDuration() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE_CEST);
        Map<ZonedDateTime, Integer> result = converter.extractSunshineDuration(image, toUTC(START_DATE_CEST));

        assertNotNull(result);
        assertFalse(result.isEmpty());
        assertEquals(48, result.size());

        // Verify specific hourly values from testSunMinutes (hours 2-23 of day 1)
        assertEquals(0, result.get(fromBerlinTimeToUTC(START_DATE_CEST,2)));
        assertEquals(2, result.get(fromBerlinTimeToUTC(START_DATE_CEST, 9)));
        assertEquals(26, result.get(fromBerlinTimeToUTC(START_DATE_CEST,10)));
        assertEquals(26, result.get(fromBerlinTimeToUTC(START_DATE_CEST,11)));
        assertEquals(4, result.get(fromBerlinTimeToUTC(START_DATE_CEST, 12)));
        assertEquals(9, result.get(fromBerlinTimeToUTC(START_DATE_CEST, 13)));
        assertEquals(11, result.get(fromBerlinTimeToUTC(START_DATE_CEST,14)));

        // Verify hours 0 and 1 of next day are included
        LocalDate nextDay = START_DATE_CEST.plusDays(1);
        assertTrue(result.containsKey(fromBerlinTimeToUTC(nextDay, 0)));
        assertTrue(result.containsKey(fromBerlinTimeToUTC(nextDay, 1)));
    }

    private ZonedDateTime fromBerlinTimeToUTC(LocalDate startDate, int hours)
    {
        return LocalDateTime.of(startDate, LocalTime.of(hours, 0)).atZone(BERLIN).withZoneSameInstant(UTC);
    }

    private ZonedDateTime toUTC(LocalDate startDate)
    {
        return LocalDateTime.of(startDate, LocalTime.of(0, 0)).atZone(UTC);
    }

    @Test
    void testExportToCSV() throws IOException
    {
        String csv = converter.exportToCSV(toUTC(START_DATE_CEST), TEST_IMAGE_CEST);
        assertNotNull(csv);
        assertFalse(csv.contains("."));

        String[] lines = csv.split("\n");
        assertEquals(2, lines.length);

        // First line: date 10/24 with hours 2-23 (22 values)
        String[] values1 = lines[0].split(",");
        assertEquals(25, values1.length); // date + 22 hour values
        assertEquals("2025/10/24", values1[0]);

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
    }

    @Test
    void testTotalSunshineHours() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE_CEST);
        Map<ZonedDateTime, Integer> result = converter.extractSunshineDuration(image, toUTC(START_DATE_CEST));
        LocalDate day2 = START_DATE_CEST.plusDays(1);

        assertEquals(1.6 * 60, getTotalMinutes(result, toUTC(START_DATE_CEST)), 6);
        assertEquals(1.2 * 60, getTotalMinutes(result, toUTC(day2)), 6);
    }

    private static int getTotalMinutes(Map<ZonedDateTime, Integer> result, ZonedDateTime day)
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
        BufferedImage image = converter.loadImage(TEST_IMAGE_CEST);
        LocalDate day2 = START_DATE_CEST.plusDays(1);
        LocalDate day3 = START_DATE_CEST.plusDays(2);

        Map<ZonedDateTime, Integer> result = converter.extractSunshineDuration(image, toUTC(START_DATE_CEST));
        assertEquals(48, result.size()); // startDate and 2 now
        assertTrue(result.containsKey(fromBerlinTimeToUTC(START_DATE_CEST, 2)), "day1 first hour");
        assertTrue(result.containsKey(fromBerlinTimeToUTC(day2, 2)), "day2 first hour");
        assertTrue(result.containsKey(fromBerlinTimeToUTC(day3, 1)), "day2 last hour on day3");

        // Verify specific hourly values from testSunMinutes (hours 2-23 of day 1)
        assertEquals(0, result.get(fromBerlinTimeToUTC(day2, 2)));
        assertEquals(9, result.get(fromBerlinTimeToUTC(day2, 9)));
        assertEquals(14, result.get(fromBerlinTimeToUTC(day2,10)));
        assertEquals(18, result.get(fromBerlinTimeToUTC(day2,11)));
        assertEquals(14, result.get(fromBerlinTimeToUTC(day2, 12)));
        assertEquals(14, result.get(fromBerlinTimeToUTC(day2, 13)));
        assertEquals(7, result.get(fromBerlinTimeToUTC(day2, 14)));
        assertEquals(0, result.get(fromBerlinTimeToUTC(day2, 15)));
    }

    @Test
    void testCET() throws IOException
    {
        BufferedImage image = converter.loadImage(TEST_IMAGE_CET);

        Map<ZonedDateTime, Integer> result = converter.extractSunshineDuration(image, toUTC(START_DATE_CET));
        assertEquals(48, result.size()); // startDate and 2 now
        assertTrue(result.containsKey(fromBerlinTimeToUTC(START_DATE_CET, 1)), "day1 first hour");
        assertTrue(result.containsKey(fromBerlinTimeToUTC(START_DATE_CET.plusDays(1), 2)), "day2 first hour");

        // Verify specific hourly values from testSunMinutes (hours 2-23 of day 1)
        assertEquals(0, result.get(fromBerlinTimeToUTC(START_DATE_CET, 1)));
        assertEquals(0, result.get(fromBerlinTimeToUTC(START_DATE_CET, 6)));
        assertEquals(4, result.get(fromBerlinTimeToUTC(START_DATE_CET, 7)));
        assertEquals(21, result.get(fromBerlinTimeToUTC(START_DATE_CET, 8)));
        assertEquals(7, result.get(fromBerlinTimeToUTC(START_DATE_CET, 9)));
        assertEquals(6, result.get(fromBerlinTimeToUTC(START_DATE_CET, 10)));
        assertEquals(43, result.get(fromBerlinTimeToUTC(START_DATE_CET, 11)));
        assertEquals(31, result.get(fromBerlinTimeToUTC(START_DATE_CET, 12)));
        assertEquals(11, result.get(fromBerlinTimeToUTC(START_DATE_CET, 13)));
        assertEquals(0, result.get(fromBerlinTimeToUTC(START_DATE_CET, 14)));
        assertEquals(31, result.get(fromBerlinTimeToUTC(START_DATE_CET, 15)));
        assertEquals(0, result.get(fromBerlinTimeToUTC(START_DATE_CET, 16)));
        assertEquals(19, result.get(fromBerlinTimeToUTC(START_DATE_CET, 17)));
        assertEquals(0, result.get(fromBerlinTimeToUTC(START_DATE_CET, 18)));
    }
}
