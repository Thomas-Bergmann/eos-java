package de.hatoka.eos.devices.internal.business.metrics;

import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.devices.capi.business.metrics.ForecastMetricsExporter;
import de.hatoka.eos.devices.internal.business.DateTooling;
import de.hatoka.eos.devices.internal.business.config.ConfigurationLoader;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.ZonedDateTime;

@QuarkusTest
public class PriceForecastExporterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(PriceForecastExporterTest.class);

    @Inject
    private ConfigurationLoader configurationLoader;

    @Inject
    private ForecastMetricsExporter metricsExporter;

    @Test
    public void testExportEnergyPrice() throws IOException
    {
        // Arrange - Load test configuration without electric car for cleaner curves
        InstallationConfig installationConfig = configurationLoader.load("test-installation-for-grafana.yaml");
        EnergyPriceForecast priceProvider = installationConfig.getGrid().getEnergyPriceProvider();
        // Simulate from August, because of retrieved daily prices (15min interval)
        ZonedDateTime startDate = DateTooling.createBerlinDate("2025/08/09");
        ZonedDateTime endDate = startDate.plusDays(3);
        Duration stepDuration = Duration.ofMinutes(15);

        ZonedDateTime currentDate = startDate;
        while(currentDate.isBefore(endDate))
        {
            metricsExporter.export(currentDate, priceProvider);
            currentDate = currentDate.plus(stepDuration);
        }

        // Export to InfluxDB/Grafana
        if (metricsExporter instanceof InfluxDBMetricsExporter influxDBMetricsExporter && !influxDBMetricsExporter.isAvailable())
        {
            LOGGER.warn("⚠️ InfluxDB exporter not available. Start with: docker-compose up -d");
            LOGGER.info("💡 To enable metrics export:");
            LOGGER.info("   1. Run: docker-compose up -d");
            LOGGER.info("   2. Wait for services to start");
            LOGGER.info("   3. Re-run this test");
            LOGGER.info("   4. Open Grafana: http://localhost:3000 (admin/admin123)");
        }
    }
}
