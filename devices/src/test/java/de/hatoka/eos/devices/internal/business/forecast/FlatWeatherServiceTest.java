package de.hatoka.eos.devices.internal.business.forecast;

import de.hatoka.eos.devices.capi.business.forecast.WeatherForecast;
import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.devices.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class FlatWeatherServiceTest
{
    @Test
    public void testBerlin()
    {
        // Arrange - Winter in Berlin: short daylight, late sunrise, early sunset
        WeatherForecast service = FlatWeatherService.FULL_FROM_7_to_18;
        List<Integer> noSunHours = List.of(0, 1, 2, 3, 4, 5, 6, 7, 18, 19, 20, 21, 22, 23);
        Map<Integer, Integer> expectedSunMinutes = Map.of(
                        8, 54,   // 8am - some sun
                        9, 60,  // 9am - some sun
                        10, 60, // 10am - some sun
                        11, 60, // 11am - more sun
                        12, 60, // noon - peak sun
                        13, 60, // 1pm - less than peak
                        14, 60, // 2pm - less than peak
                        15, 60, // 3pm - some sun
                        16, 60, // 4pm - some sun
                        17, 54
        );
        noSunHours.forEach(key -> assertEquals(Percentage.ZERO, service.getSunProbability(DateTooling.createBerlinDate("2024/12/21").withHour(key)),
                        "Sun minutes mismatch for 21.12 at " + key + ":00"));
        // Act & Assert
        expectedSunMinutes.keySet()
                          .stream()
                          .sorted()
                          .forEach(hour -> assertEquals(map(expectedSunMinutes.get(hour)),
                                          service.getSunProbability(DateTooling.createBerlinDate("2024/12/21").withHour(hour)),
                                          "Sun minutes mismatch for 21.12 at " + hour + ":00"));
        expectedSunMinutes.keySet()
                          .stream()
                          .sorted()
                          .forEach(hour -> assertEquals(map(expectedSunMinutes.get(hour)),
                                          service.getSunProbability(DateTooling.createBerlinDate("2024/7/21").withHour(hour)),
                                          "Sun minutes mismatch for 21.07 at " + hour + ":00"));
    }

    private Percentage map(int minutes)
    {
        return new Percentage(minutes / 60.0); // Convert minutes to percentage (0-100)
    }
}