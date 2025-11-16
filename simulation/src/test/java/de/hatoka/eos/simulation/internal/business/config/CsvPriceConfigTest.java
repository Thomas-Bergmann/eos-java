package de.hatoka.eos.simulation.internal.business.config;

import de.hatoka.eos.simulation.capi.business.config.CsvPriceConfig;
import de.hatoka.eos.simulation.capi.business.config.GridConfig;
import de.hatoka.eos.simulation.capi.business.config.InstallationConfig;
import de.hatoka.eos.units.capi.Money;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@QuarkusTest
public class CsvPriceConfigTest
{
    @Inject
    private ConfigurationLoader configurationLoader;

    @Test
    public void testCsvPriceConfigLoading() throws IOException
    {
        InstallationConfig config = configurationLoader.load("test-installation-for-csv-prices.yaml");
        
        assertNotNull(config);
        assertNotNull(config.getGrid());
        
        GridConfig gridConfig = config.getGrid();
        assertNotNull(gridConfig.csvPriceProvider());
        
        CsvPriceConfig csvPriceConfig = gridConfig.csvPriceProvider();
        assertNotNull(csvPriceConfig.resource());
        assertFalse(csvPriceConfig.resource().isEmpty());
        
        assertEquals("stockprices_DE_LU_week_32-33.csv", csvPriceConfig.resource().getFirst());
        
        assertNotNull(csvPriceConfig.importCharge());
        assertNotNull(csvPriceConfig.exportCharge());
        
        assertEquals("EUR", csvPriceConfig.currency());
        assertEquals(Money.ofEur(0.08), csvPriceConfig.importCharge().price());
        assertEquals(Money.ofEur(0.04), csvPriceConfig.exportCharge().price());
    }

    @Test
    public void testCsvStockEnergyPriceProviderCreation() throws IOException
    {
        InstallationConfig config = configurationLoader.load("test-installation-for-csv-prices.yaml");
        GridConfig gridConfig = config.getGrid();
        
        de.hatoka.eos.simulation.capi.business.forecast.EnergyPriceForecast priceProvider = gridConfig.getEnergyPriceProvider();
        
        assertNotNull(priceProvider);
        assertInstanceOf(de.hatoka.eos.simulation.internal.business.forecast.CsvStockEnergyPriceProvider.class, priceProvider);
        
        // Test actual price retrieval for a known date/time from the CSV
        // CSV contains data for 2025/08/04 at hour 0 with price 81.05 EUR/MWh
        java.time.ZoneId zone = java.time.ZoneId.of("Europe/Berlin");
        java.time.ZonedDateTime testTime = java.time.LocalDate.of(2025, 8, 4)
            .atStartOfDay(zone); // Hour 0
        
        Money importPrice = priceProvider.getImportPrice(testTime);
        Money exportPrice = priceProvider.getExportPrice(testTime);
        
        assertNotNull(importPrice);
        assertNotNull(exportPrice);
        
        // Import price should be market price (81.05/1000 = 0.08105) + import charge (0.08)
        // Export price should be market price (81.05/1000 = 0.08105) - export charge (0.04) 
        // Market price is higher than export charge, so export price should be positive
        
        assertTrue(importPrice.amount().doubleValue() > 0.08, 
                  "Import price should include market price + charges: " + importPrice.amount());
        assertTrue(exportPrice.amount().doubleValue() > 0.0, 
                  "Export price should be positive: " + exportPrice.amount());
        
        // Import price should be higher than export price (as is typical)
        assertTrue(importPrice.amount().doubleValue() > exportPrice.amount().doubleValue(),
                  "Import price should be higher than export price");
        
        // Verify currency is correctly applied
        assertEquals("EUR", importPrice.currencyMnemonic());
        assertEquals("EUR", exportPrice.currencyMnemonic());
    }
}