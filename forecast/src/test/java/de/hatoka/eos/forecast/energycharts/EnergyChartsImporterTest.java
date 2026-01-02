package de.hatoka.eos.forecast.energycharts;

import de.hatoka.eos.persistence.capi.energystock.EnergyStockDao;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockKey;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockPO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class EnergyChartsImporterTest
{
    @Inject
    private EnergyChartsImporter importer;
    @Inject
    private EnergyStockDao dao;

    @Test
    void testImportFromValidUrl() throws IOException, InterruptedException
    {
        // this importer can only import data of today and tomorrow
        ZonedDateTime startDate = ZonedDateTime.now().toLocalDate().atStartOfDay(ZoneId.of("Europe/Berlin"));
        importer.importStockData(startDate);
        EnergyStockPO retrieved = dao.get(EnergyStockKey.valueOf(startDate.plusHours(1)));
        assertNotNull(retrieved);
        assertNotNull(retrieved.getDayAheadPrice());
    }
}
