package de.hatoka.eos.devices.internal.business.config;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.units.Money;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationLoaderTest
{
    @Test
    public void testLoadFromResource() throws IOException
    {
        // Arrange
        ConfigurationLoader loader = new ConfigurationLoader();

        // Act
        InstallationConfig config = loader.load("test-installation-with-car.yaml");

        // Assert
        assertNotNull(config);
        assertNotNull(config.getInstallation());
        assertEquals("Europe/Germany/Thuringia/Jena", config.getInstallation().getLocation());
        assertEquals("Europe/Berlin", config.getInstallation().getTimezone());

        // Test devices
        assertNotNull(config.getDevices());
        // five sections of devices: solar panels, noisy usage, batteries, electric cars and grid
        assertEquals(5, config.getDevices().size());

        DeviceConfig solarPanel = config.getDevices().getFirst();
        assertEquals("SOLAR_PANEL", solarPanel.getType().name());
        assertNotNull(solarPanel.getProduction());
        assertEquals(0.45, solarPanel.getProduction().amount());
        assertEquals("K_W", solarPanel.getProduction().unit().name());
        assertEquals(13, solarPanel.getCount());

        DeviceConfig battery = config.getDevices().get(2);
        assertEquals("BATTERY", battery.getType().name());
        assertNotNull(battery.getCapacity());
        assertEquals(3.0, battery.getCapacity().amount());
        assertEquals("K_WH", battery.getCapacity().unit().name());
        assertNotNull(battery.getChargeRate());
        assertEquals(2.2, battery.getChargeRate().amount());
        assertEquals("K_W", battery.getChargeRate().unit().name());
        assertNotNull(battery.getDischargeRate());
        assertEquals(2.1, battery.getDischargeRate().amount());
        assertEquals("K_W", battery.getDischargeRate().unit().name());
        assertEquals(0.05, battery.getDailyStorageLoss().value(), 0.001);
        assertEquals(0.85, battery.getChargingEfficiency().value(), 0.001);
        assertEquals(0.85, battery.getDischargingEfficiency().value(), 0.001);
        // Test grid config
        assertNotNull(config.getGrid());
        assertNotNull(config.getGrid().flatPriceConfig());
        assertEquals(Money.ofEur(0.39), config.getGrid().flatPriceConfig().importPrice());
        assertEquals(Money.ofEur(0.08), config.getGrid().flatPriceConfig().exportPrice());

        // Test charging config
        assertNotNull(config.getCharging());
        assertNotNull(config.getCharging().getForceChargingLimit());
        assertEquals(10, config.getCharging().getForceChargingLimit().toPercentage());
        assertNotNull(config.getCharging().getCarChargingLimit());
        assertEquals(80, config.getCharging().getCarChargingLimit().toPercentage());
    }
}