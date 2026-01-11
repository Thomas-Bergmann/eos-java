package de.hatoka.eos.hardware.enphase.usage;

import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import de.hatoka.eos.units.capi.Energy;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class EnphaseTelemetryTest
{
    @Inject
    private EnphaseTelemetry usage;

    @Test
    public void testConsumption()
    {
        ZonedDateTime from = ZonedDateTime.now().minusDays(3).withZoneSameInstant(ZoneId.of("Europe/Berlin"));
        Map<UsageKey, UsagePO> usedNow = usage.getDailyUsage(from);
        assertEquals(96, usedNow.size());
        UsagePO firstValue = usedNow.entrySet().stream().toList().getFirst().getValue();
        Energy positiveValues = firstValue.getProduction().add(firstValue.getGridImport()).add(firstValue.getDischarge());
        Energy negativeValues = firstValue.getConsumption().add(firstValue.getGridExport()).add(firstValue.getCharge());
        assertEquals(positiveValues, negativeValues.negate());
        usedNow.forEach((k, value) -> {
            Energy positive = value.getProduction().add(value.getGridImport()).add(value.getDischarge());
            Energy negative = value.getConsumption().add(value.getGridExport()).add(value.getCharge());
            assertEquals(positive, negative.negate());
        });
    }
}

