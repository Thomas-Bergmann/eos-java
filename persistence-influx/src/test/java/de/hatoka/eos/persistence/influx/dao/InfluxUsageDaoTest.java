package de.hatoka.eos.persistence.influx.dao;

import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Percentage;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Integration test for InfluxEnergyStockDao.
 *
 * This test requires InfluxDB to be running (e.g., via docker-compose).
 * The test uses a dedicated test bucket to avoid interfering with production data.
 */
@QuarkusTest
class InfluxUsageDaoTest
{
    @Inject
    InfluxUsageDao dao;

    // Test data constants
    private static final ZonedDateTime TEST_TIME = ZonedDateTime.of(2024, 1, 11, 14, 0, 0, 0, ZoneId.of("UTC"));

    @AfterEach
    void tearDown()
    {
        dao.delete(getKey(TEST_TIME));
        dao.delete(getKey(TEST_TIME.plusHours(1)));
        dao.delete(getKey(TEST_TIME.minusHours(1)));
    }

    private static UsagePO createRecord(Energy energy)
    {
        UsagePO result = new UsagePO();
        result.setProduction(energy);
        result.setConsumption(energy.add(Energy.ofWh(10)));
        result.setGridImport(energy.add(Energy.ofWh(20)));
        result.setGridExport(energy.add(Energy.ofWh(30)));
        result.setCharge(energy.add(Energy.ofWh(40)));
        result.setDischarge(energy.add(Energy.ofWh(50)));
        result.setBatteryState(Percentage.valueOfPercent(30));
        return result;
    }

    private static UsageKey getKey(ZonedDateTime time)
    {
        return UsageKey.valueOf(time);
    }

    @Test
    void shouldStoreAndRetrieve()
    {
        // Given
        UsagePO expected = createRecord(Energy.ofWh(234));

        // When
        dao.update(getKey(TEST_TIME), expected);

        // Then
        UsagePO actual = dao.get(getKey(TEST_TIME));

        assertNotNull(actual, "Retrieved should not be null");
        assertEquals(expected, actual);
    }
}

