package de.hatoka.eos.persistence.capi.weather;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for WeatherForecastKey to verify equality and hashCode behavior,
 * especially with ZonedDateTime instances.
 */
class WeatherForecastKeyTest
{
    @Test
    void testEqualityWithSameInstances()
    {
        // Given
        ZonedDateTime time = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);

        // Then
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    void testEqualityWithDifferentZonedDateTimeInstances_SameInstant()
    {
        // Given - Create two separate ZonedDateTime instances representing the same instant
        ZonedDateTime time1 = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        ZonedDateTime time2 = ZonedDateTime.parse("2024-01-01T12:00:00Z");

        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time1, WeatherDataSource.OPENMETEO);
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time2, WeatherDataSource.OPENMETEO);

        // Then - They should be equal even though they are different object instances
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }

    @Test
    void testEqualityWithDifferentTimeZones_ButSameTime()
    {
        // Given - Same instant but different time zones
        ZonedDateTime timeUTC = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        ZonedDateTime timePlusOne = ZonedDateTime.parse("2024-01-01T13:00:00+01:00");

        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, timeUTC, WeatherDataSource.OPENMETEO);
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, timePlusOne, WeatherDataSource.OPENMETEO);

        // Then - They should be equal (because it's at the same moment in time)
        assertEquals(key1, key2);
    }

    @Test
    void testEqualityWithDifferentInstants()
    {
        // Given
        ZonedDateTime time1 = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        ZonedDateTime time2 = ZonedDateTime.parse("2024-01-01T13:00:00Z");

        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time1, WeatherDataSource.OPENMETEO);
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time2, WeatherDataSource.OPENMETEO);

        // Then
        assertNotEquals(key1, key2);
    }

    @Test
    void testEqualityWithDifferentStations()
    {
        // Given
        ZonedDateTime time = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.LEIPZIG_STADTWERKE, time, WeatherDataSource.OPENMETEO);

        // Then
        assertNotEquals(key1, key2);
    }

    @Test
    void testEqualityWithDifferentSources()
    {
        // Given
        ZonedDateTime time = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.TEST);

        // Then
        assertNotEquals(key1, key2);
    }

    @Test
    void testAsMapKey()
    {
        // Given - Simulate how it's used in MemoryWeatherForecastDao
        Map<WeatherForecastKey, String> map = new HashMap<>();

        ZonedDateTime time = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);

        map.put(key1, "test-data");

        // When - Create a new key with same values but different ZonedDateTime instance
        ZonedDateTime time2 = ZonedDateTime.parse("2024-01-01T12:00:00Z");
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time2, WeatherDataSource.OPENMETEO);

        // Then - Should find the value using the new key
        assertTrue(map.containsKey(key2));
        assertEquals("test-data", map.get(key2));
    }

    @Test
    void testAsMapKey_WithParsingFromString()
    {
        // Store with a ZonedDateTime created one way
        ZonedDateTime storedTime = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.of("UTC")).plusHours(1);
        WeatherForecastKey storedKey = WeatherForecastKey.valueOf(WeatherStation.APOLDA, storedTime, WeatherDataSource.OPENMETEO);

        // Retrieve with a ZonedDateTime created from parsing (like from API)
        ZonedDateTime parsedTime = ZonedDateTime.parse(storedTime.format(DateTimeFormatter.ISO_ZONED_DATE_TIME), DateTimeFormatter.ISO_ZONED_DATE_TIME);
        assertEquals(storedTime, parsedTime, "ZonedDateTime instances should be equal");

        WeatherForecastKey retrieveKey = WeatherForecastKey.valueOf(WeatherStation.APOLDA, parsedTime, WeatherDataSource.OPENMETEO);

        // Then - Should find the value
        assertEquals(storedKey, retrieveKey, "Keys should be equal");
    }

    @Test
    void testAsMapKey_WithAddingZ()
    {
        // Given - This simulates the OpenMeteo parsing scenario
        Map<WeatherForecastKey, String> map = new HashMap<>();

        // Store with standard UTC time
        ZonedDateTime storedTime = ZonedDateTime.parse("2024-01-01T12:00:00Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        WeatherForecastKey storedKey = WeatherForecastKey.valueOf(WeatherStation.APOLDA, storedTime, WeatherDataSource.OPENMETEO);
        map.put(storedKey, "test-data");

        // Retrieve by parsing time string with Z appended (like OpenMeteo does)
        String timeWithoutZ = "2024-01-01T12:00:00";
        ZonedDateTime parsedTime = ZonedDateTime.parse(timeWithoutZ + "Z", DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        WeatherForecastKey retrieveKey = WeatherForecastKey.valueOf(WeatherStation.APOLDA, parsedTime, WeatherDataSource.OPENMETEO);

        // Then - Should find the value
        assertEquals(storedTime, parsedTime, "ZonedDateTime instances should be equal");
        assertEquals(storedKey, retrieveKey, "Keys should be equal");
        assertTrue(map.containsKey(retrieveKey), "Map should contain the key");
        assertEquals("test-data", map.get(retrieveKey));
    }

    @Test
    void testValueOfFactory()
    {
        // Given
        ZonedDateTime time = ZonedDateTime.parse("2024-01-01T12:00:00Z").withZoneSameInstant(ZoneId.of("UTC"));

        // When
        WeatherForecastKey key = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);

        // Then
        assertEquals(WeatherStation.APOLDA, key.station());
        assertEquals(time.toInstant().toEpochMilli(), key.time());
        assertEquals(time, key.getZonedDateTime());
        assertEquals(WeatherDataSource.OPENMETEO, key.source());
    }

    @Test
    void testValueOfFactory_Equality()
    {
        // Given
        ZonedDateTime time = ZonedDateTime.parse("2024-01-01T12:00:00Z");

        // When
        WeatherForecastKey key1 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);
        WeatherForecastKey key2 = WeatherForecastKey.valueOf(WeatherStation.APOLDA, time, WeatherDataSource.OPENMETEO);

        // Then
        assertEquals(key1, key2);
        assertEquals(key1.hashCode(), key2.hashCode());
    }
}

