package de.hatoka.eos.hardware.enphase.usage;

import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class EnphaseUsageTest
{
    @Inject
    private EnphaseUsage usage;

    @Test
    public void testConsumption()
    {
        Map<UsageKey, UsagePO> usedNow = usage.getDailyUsage(ZonedDateTime.now().minusDays(10), ZonedDateTime.now());
        assertEquals(10, usedNow.size());
    }
}

