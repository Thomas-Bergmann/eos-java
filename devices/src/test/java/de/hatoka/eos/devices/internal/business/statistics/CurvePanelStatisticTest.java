package de.hatoka.eos.devices.internal.business.statistics;

import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.devices.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CurvePanelStatisticTest
{
    @Test
    public void testBerlin()
    {
        // Arrange - Winter in Berlin: short daylight, late sunrise, early sunset
        CurvePanelStatistic service = new CurvePanelStatistic();
        List<Integer> noSunHours = List.of(0, 1, 2, 3, 4, 5, 6, 7, 8, 17, 18, 19, 20, 21, 22, 23);
        Map<Integer, Double> expectedEfficiency = Map.of(
                        9, 0.29,  // 9am - some sun
                        10, 0.54, // 10am - some sun
                        11, 0.74, // 11am - more sun
                        12, 0.83, // noon - peak sun
                        13, 0.83, // 1pm - less than peak
                        14, 0.74, // 2pm - less than peak
                        15, 0.54, // 3pm - some sun
                        16, 0.29 // 4pm - some sun
        );
        noSunHours.forEach(hour -> assertEquals(Percentage.ZERO, service.getEfficiency(DateTooling.createBerlinDate("2024/12/21").withHour(hour)),
                        "Efficiency mismatch for 21.12 at " + hour + ":00"));
        // Act & Assert
        expectedEfficiency.keySet()
                          .stream()
                          .sorted()
                          .forEach(hour -> assertEquals(expectedEfficiency.get(hour),
                                          service.getEfficiency(DateTooling.createBerlinDate("2024/12/21").withHour(hour)).value(), 0.05,
                                          "Efficiency mismatch for 21.12 at " + hour + ":00"));
        expectedEfficiency.keySet()
                          .stream()
                          .sorted()
                          .forEach(hour -> assertEquals(expectedEfficiency.get(hour),
                                          service.getEfficiency(DateTooling.createBerlinDate("2024/07/21").withHour(hour)).value(), 0.05,
                                          "Efficiency mismatch for 21.07 at " + hour + ":00"));
    }
}