package de.hatoka.eos.persistence.memory;

import de.hatoka.eos.persistence.capi.energystock.EnergyStockKey;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockPO;
import de.hatoka.eos.units.capi.Money;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.*;

class MemoryEnergyStockDaoTest
{
    private MemoryEnergyStockDao dao;

    @BeforeEach
    void setUp()
    {
        dao = new MemoryEnergyStockDao();
    }

    @Test
    void testUpdateAndGet()
    {
        // Given
        EnergyStockKey key = EnergyStockKey.valueOf(ZonedDateTime.now());
        EnergyStockPO data = new EnergyStockPO();
        data.setDayAheadPrice(Money.ofEur(0.12));

        // When
        dao.update(key, data);
        EnergyStockPO result = dao.get(key);

        // Then
        assertNotNull(result);
        assertEquals(0.12, result.getDayAheadPrice().amount().doubleValue(), 0.001);
        assertEquals("EUR", result.getDayAheadPrice().currencyMnemonic());
    }

    @Test
    void testGetNonExistent()
    {
        // Given
        EnergyStockKey key = EnergyStockKey.valueOf(ZonedDateTime.now());

        // When
        EnergyStockPO result = dao.get(key);

        // Then
        assertNull(result);
    }

    @Test
    void testDelete()
    {
        // Given
        EnergyStockKey key = EnergyStockKey.valueOf(ZonedDateTime.now());
        EnergyStockPO data = new EnergyStockPO();
        data.setDayAheadPrice(Money.ofEur(0.12));
        dao.update(key, data);

        // When
        dao.delete(key);
        EnergyStockPO result = dao.get(key);

        // Then
        assertNull(result);
    }

    @Test
    void testClear()
    {
        // Given
        EnergyStockKey key1 = EnergyStockKey.valueOf(ZonedDateTime.now());
        EnergyStockKey key2 = EnergyStockKey.valueOf(ZonedDateTime.now().plusHours(1));
        EnergyStockPO data = new EnergyStockPO();
        data.setDayAheadPrice(Money.ofEur(0.12));
        dao.update(key1, data);
        dao.update(key2, data);

        // When
        dao.clear();

        // Then
        assertNull(dao.get(key1));
        assertNull(dao.get(key2));
    }
}

