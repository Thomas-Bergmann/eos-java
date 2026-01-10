package de.hatoka.eos.optimization.capi.tasks;

import de.hatoka.eos.simulation.capi.business.simulation.DeviceManipulator;
import de.hatoka.eos.units.capi.Percentage;
import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for CarCharge task, specifically the evolute() method.
 */
public class CarChargeTest
{
    private CarCharge from(DeviceManipulator manipulator)
    {
        if (manipulator instanceof CarCharge carCharge)
        {
            return carCharge;
        }
        return null;
    }

    @Test
    public void testEvoluteReturnsNeighbors()
    {
        ZonedDateTime from = ZonedDateTime.parse("2026-01-02T08:00:00Z");
        ZonedDateTime to = ZonedDateTime.parse("2026-01-02T18:00:00Z");
        ZonedDateTime start = ZonedDateTime.parse("2026-01-02T12:00:00Z");

        CarCharge carCharge = CarCharge.valueOf(from, to, start, 5);

        List<DeviceManipulator> neighbors = carCharge.evolute();
        assertEquals(4, neighbors.size(), "Expected 4 - 2 dimensions - start time and duration");
        // first plus one hour
        assertEquals(ZonedDateTime.parse("2026-01-02T13:00:00Z").toInstant().toEpochMilli(), from(neighbors.get(0)).start());
        // second minus one hour
        assertEquals(ZonedDateTime.parse("2026-01-02T11:00:00Z").toInstant().toEpochMilli(), from(neighbors.get(1)).start());
        // third plus one hour in duration
        assertEquals(6, from(neighbors.get(2)).hours());
        // third minus one hour in duration
        assertEquals(4, from(neighbors.get(3)).hours());
    }
}

