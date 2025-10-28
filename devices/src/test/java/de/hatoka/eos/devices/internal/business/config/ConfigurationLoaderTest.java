package de.hatoka.eos.devices.internal.business.config;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.business.config.SimulationConfig;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.capi.units.Money;
import de.hatoka.eos.devices.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigurationLoaderTest
{
    private final ConfigurationLoader loader = new ConfigurationLoader();

    @Test
    public void testInstallationConfigFromResource() throws IOException
    {
        // Act
        InstallationConfig config = loader.loadInstallation("test-installation-with-car.yaml");

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

        // Test device-specific charging limits
        DeviceConfig batteryConfig = config.getDevices().stream().filter(device -> device.getType() == DeviceType.BATTERY).findFirst().orElse(null);
        assertNotNull(batteryConfig);
        assertNotNull(batteryConfig.getForceChargingLimit());
        assertEquals(10, batteryConfig.getForceChargingLimit().toPercentage());

        DeviceConfig carConfig = config.getDevices().stream().filter(device -> device.getType() == DeviceType.ELECTRIC_CAR).findFirst().orElse(null);
        assertNotNull(carConfig);
        assertNotNull(carConfig.getForceChargingLimit());
        assertEquals(80, carConfig.getForceChargingLimit().toPercentage());
    }

    @Test
    public void testSimConfig() throws IOException
    {
        SimulationConfig simConfig = loader.loadSimulation("test-simulation-with-csv-prices.yaml");

        assertNotNull(simConfig);
        assertEquals(ZoneId.of("Europe/Berlin"), simConfig.getTimeSettings().getTimezone());
        assertEquals(DateTooling.createBerlinDate("2025/08/04"), simConfig.getTimeSettings().getZonedStartTime());
        assertEquals(Duration.ofHours(1), simConfig.getTimeSettings().getStepDuration());

        assertEquals(Duration.ofMinutes(15), loader.loadSimulation("test-simulation-summer.yaml").getTimeSettings().getStepDuration());
    }
}