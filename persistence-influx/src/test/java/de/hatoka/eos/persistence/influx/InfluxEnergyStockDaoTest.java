package de.hatoka.eos.persistence.influx;

import de.hatoka.eos.persistence.capi.energystock.EnergyStockKey;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockPO;
import de.hatoka.eos.units.capi.Money;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test for InfluxEnergyStockDao.
 *
 * This test requires InfluxDB to be running (e.g., via docker-compose).
 * The test uses a dedicated test bucket to avoid interfering with production data.
 */
@QuarkusTest
class InfluxEnergyStockDaoTest
{
    @Inject
    InfluxEnergyStockDao dao;

    // Test data constants
    private static final ZonedDateTime TEST_TIME = ZonedDateTime.of(2024, 1, 15, 12, 0, 0, 0, ZoneId.of("UTC"));
    private static final Money TEST_PRICE = Money.ofEur(50.0); // 50 EUR
    private static final Money UPDATED_PRICE = Money.ofEur(75.0); // 75 EUR

    @AfterEach
    void tearDown()
    {
        dao.delete(getKey(TEST_TIME));
        dao.delete(getKey(TEST_TIME.plusHours(1)));
        dao.delete(getKey(TEST_TIME.minusHours(1)));
    }

    private static EnergyStockPO createEnergyStock(Money dayAheadPrice)
    {
        EnergyStockPO energyStock = new EnergyStockPO();
        energyStock.setDayAheadPrice(dayAheadPrice);
        return energyStock;
    }

    private static EnergyStockKey getKey(ZonedDateTime time)
    {
        return EnergyStockKey.valueOf(time);
    }

    @Test
    void shouldStoreAndRetrieveEnergyStockData()
    {
        // Given
        EnergyStockPO energyStock = createEnergyStock(TEST_PRICE);

        // When
        dao.update(getKey(TEST_TIME), energyStock);

        // Then
        EnergyStockPO retrievedStock = dao.get(getKey(TEST_TIME));

        assertNotNull(retrievedStock, "Retrieved energy stock should not be null");
        assertNotNull(retrievedStock.getDayAheadPrice(), "Day ahead price should not be null");
        assertEquals(TEST_PRICE.amount().doubleValue(), retrievedStock.getDayAheadPrice().amount().doubleValue(), 0.001,
                "Day ahead price amount should match the stored value");
        assertEquals(TEST_PRICE.currencyMnemonic(), retrievedStock.getDayAheadPrice().currencyMnemonic(),
                "Currency should match the stored value");
    }

    @Test
    void shouldUpdateExistingEnergyStockData()
    {
        // Given - store initial data
        EnergyStockPO initialStock = createEnergyStock(TEST_PRICE);
        dao.update(getKey(TEST_TIME), initialStock);

        // When - update with new data
        EnergyStockPO updatedStock = createEnergyStock(UPDATED_PRICE);
        dao.update(getKey(TEST_TIME), updatedStock);

        // Then
        EnergyStockPO retrievedStock = dao.get(getKey(TEST_TIME));

        assertNotNull(retrievedStock, "Retrieved energy stock should not be null");
        assertNotNull(retrievedStock.getDayAheadPrice(), "Day ahead price should not be null");
        assertEquals(UPDATED_PRICE.amount().doubleValue(), retrievedStock.getDayAheadPrice().amount().doubleValue(), 0.001,
                "Day ahead price should reflect the updated value");
    }

    @Test
    void shouldReturnNullWhenNoDataExists()
    {
        // Given - a time point with no data
        ZonedDateTime nonExistentTime = TEST_TIME.minusDays(365);

        // When/Then
        assertNull(dao.get(getKey(nonExistentTime)), "Retrieved energy stock should be null for non-existent data");
    }

    @Test
    void shouldHandleMultipleTimePoints()
    {
        // Given - multiple energy stocks at different times
        ZonedDateTime time1 = TEST_TIME;
        ZonedDateTime time2 = TEST_TIME.plusHours(1);
        ZonedDateTime time3 = TEST_TIME.plusHours(2);

        // When
        dao.update(getKey(time1), createEnergyStock(Money.ofEur(30.0)));
        dao.update(getKey(time2), createEnergyStock(Money.ofEur(45.0)));
        dao.update(getKey(time3), createEnergyStock(Money.ofEur(60.0)));

        // Then
        assertEquals(30.0, dao.get(getKey(time1)).getDayAheadPrice().amount().doubleValue(), 0.001);
        assertEquals(45.0, dao.get(getKey(time2)).getDayAheadPrice().amount().doubleValue(), 0.001);
        assertEquals(60.0, dao.get(getKey(time3)).getDayAheadPrice().amount().doubleValue(), 0.001);

        // Clean up additional test data
        dao.delete(getKey(time2));
        dao.delete(getKey(time3));
    }

    @Test
    void shouldHandlePreciseTimeRetrieval()
    {
        // Given - energy stock with precise timestamp
        ZonedDateTime preciseTime = TEST_TIME.truncatedTo(ChronoUnit.SECONDS);
        EnergyStockPO energyStock = createEnergyStock(TEST_PRICE);

        // When
        dao.update(getKey(preciseTime), energyStock);

        // Then - should retrieve data even with slight time variations
        EnergyStockPO retrieved = dao.get(getKey(preciseTime));

        assertNotNull(retrieved.getDayAheadPrice());
        assertEquals(TEST_PRICE.amount().doubleValue(), retrieved.getDayAheadPrice().amount().doubleValue(), 0.001);
    }

    @Test
    void shouldHandleBoundaryPriceValues()
    {
        // Test with zero price
        EnergyStockPO stock0 = createEnergyStock(Money.ZERO);
        dao.update(getKey(TEST_TIME), stock0);

        // Test with high price
        ZonedDateTime timeHigh = TEST_TIME.plusMinutes(5);
        EnergyStockPO stockHigh = createEnergyStock(new Money(BigDecimal.valueOf(1000.0), "EUR"));
        dao.update(getKey(timeHigh), stockHigh);

        // Verify boundary values
        assertEquals(0.0, dao.get(getKey(TEST_TIME)).getDayAheadPrice().amount().doubleValue(), 0.001);
        assertEquals(1000.0, dao.get(getKey(timeHigh)).getDayAheadPrice().amount().doubleValue(), 0.001);

        // Clean up
        dao.delete(getKey(timeHigh));
    }

    @Test
    void shouldHandleDifferentCurrencies()
    {
        // Test with USD currency
        ZonedDateTime timeUsd = TEST_TIME.plusMinutes(10);
        EnergyStockPO stockUsd = createEnergyStock(new Money(BigDecimal.valueOf(60.0), "USD"));
        dao.update(getKey(timeUsd), stockUsd);

        // Verify currency is preserved
        EnergyStockPO retrieved = dao.get(getKey(timeUsd));
        assertNotNull(retrieved);
        assertEquals("USD", retrieved.getDayAheadPrice().currencyMnemonic());
        assertEquals(60.0, retrieved.getDayAheadPrice().amount().doubleValue(), 0.001);

        // Clean up
        dao.delete(getKey(timeUsd));
    }

    @Test
    void shouldDeleteEnergyStockData()
    {
        // Given - store some data first
        EnergyStockPO energyStock = createEnergyStock(TEST_PRICE);
        dao.update(getKey(TEST_TIME), energyStock);

        // Verify data exists
        EnergyStockPO retrievedBeforeDelete = dao.get(getKey(TEST_TIME));
        assertNotNull(retrievedBeforeDelete.getDayAheadPrice(),
                "Data should exist before deletion");

        // When - delete the data
        dao.delete(getKey(TEST_TIME));

        // Then - data should no longer exist
        assertNull(dao.get(getKey(TEST_TIME)), "Data should not exist after deletion");
    }

    @Test
    void shouldDeleteOnlySpecificTimeData()
    {
        // Given - store data at multiple time points
        ZonedDateTime time1 = TEST_TIME;
        ZonedDateTime time2 = TEST_TIME.plusHours(1);
        ZonedDateTime time3 = TEST_TIME.plusHours(2);

        dao.update(getKey(time1), createEnergyStock(Money.ofEur(20.0)));
        dao.update(getKey(time2), createEnergyStock(Money.ofEur(40.0)));
        dao.update(getKey(time3), createEnergyStock(Money.ofEur(80.0)));

        // When - delete only the middle data point
        dao.delete(getKey(time2));

        // Then - only the deleted data should be gone
        assertEquals(20.0, dao.get(getKey(time1)).getDayAheadPrice().amount().doubleValue(), 0.001);
        assertNull(dao.get(getKey(time2)), "Second data point should be deleted");
        assertEquals(80.0, dao.get(getKey(time3)).getDayAheadPrice().amount().doubleValue(), 0.001);

        // Clean up remaining data
        dao.delete(getKey(time1));
        dao.delete(getKey(time3));
    }

    @Test
    void shouldHandleDeleteOfNonExistentData()
    {
        // Given - a time point with no data
        ZonedDateTime nonExistentTime = TEST_TIME.plusDays(100);

        // When/Then - deleting non-existent data should not throw an exception
        assertDoesNotThrow(() -> dao.delete(getKey(nonExistentTime)),
                "Deleting non-existent data should not throw an exception");
    }

    @Test
    void shouldHandleNegativePrices()
    {
        // Test with negative price (can occur in energy markets)
        ZonedDateTime timeNegative = TEST_TIME.plusMinutes(15);
        EnergyStockPO stockNegative = createEnergyStock(new Money(BigDecimal.valueOf(-25.5), "EUR"));
        dao.update(getKey(timeNegative), stockNegative);

        // Verify negative value is preserved
        EnergyStockPO retrieved = dao.get(getKey(timeNegative));
        assertNotNull(retrieved);
        assertEquals(-25.5, retrieved.getDayAheadPrice().amount().doubleValue(), 0.001);

        // Clean up
        dao.delete(getKey(timeNegative));
    }
}

