package de.hatoka.eos.devices.internal.business.metrics;

import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import de.hatoka.eos.devices.capi.business.metrics.SimulationMetricsExporter;
import de.hatoka.eos.devices.capi.business.simulation.SimulationRequest;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import de.hatoka.eos.devices.capi.business.forecast.Forecasts;
import de.hatoka.eos.devices.internal.business.DateTooling;
import de.hatoka.eos.devices.internal.business.config.ConfigurationDeviceBuilder;
import de.hatoka.eos.devices.internal.business.config.ConfigurationLoader;
import de.hatoka.eos.devices.capi.business.simulation.Simulator;
import de.hatoka.eos.devices.internal.business.forecast.FlatWeatherService;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class SimulationMetricsExporterTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationMetricsExporterTest.class);
    public static final ZoneId ZONE_ID = ZoneId.of("Europe/Berlin");

    @Inject
    private ConfigurationDeviceBuilder configurationDeviceBuilder;

    @Inject
    private ConfigurationLoader configurationLoader;

    @Inject
    private Simulator simulator;

    @Inject
    private SimulationMetricsExporter metricsExporter;

    @Test
    public void testExportSimulationMetricsToGrafana() throws IOException
    {
        // Arrange - Load test configuration without electric car for cleaner curves
        InstallationConfig config = configurationLoader.load("test-installation-for-grafana.yaml");

        // Simulate from today midnight with 5-minute intervals for smooth curves
        ZonedDateTime startDate = DateTooling.createBerlinDate("2025/08/04");
        ZonedDateTime endDate = DateTooling.createBerlinDate("2025/08/17");
        Duration stepDuration = Duration.ofMinutes(15);

        LOGGER.info("🌅 Starting simulation from: {} to: {}", startDate, endDate);

        SimulationRequest request = new SimulationRequest("today-energy-simulation", startDate, endDate, stepDuration,
                        configurationDeviceBuilder.getDevices(config), Collections.emptyMap(), config.getCharging(),
                        new Forecasts(FlatWeatherService.FULL_FROM_7_to_18, config.getGrid().getEnergyPriceProvider()));

        // Act - Run simulation step by step for 5-minute interval data
        double seconds = endDate.toInstant().getEpochSecond() - startDate.toInstant().getEpochSecond();
        LOGGER.info("⚡ Running {}-hour energy simulation with {}-minute interval export...", seconds / 60 / 60, stepDuration.toMinutes());

        // Run simulation every 5 minutes for smooth curves (288 data points per day)
        SimulationResult result = simulator.simulate(request);

        // Assert - Verify results
        assertNotNull(result);
        assertNotNull(result.system());

        // Log results for verification
        LOGGER.info("=== SIMULATION RESULTS FOR GRAFANA ===");
        LOGGER.info("Solar Production: {}", result.system().produced());
        LOGGER.info("Energy Consumed: {}", result.system().consumed());
        LOGGER.info("Battery Charged: {}", result.system().charged());
        LOGGER.info("Battery Discharged: {}", result.system().discharged());
        LOGGER.info("Grid Import: {} -> {}", result.system().imported(), result.system().importRevenue());
        LOGGER.info("Grid Export: {} -> {}", result.system().exported(), result.system().exportRevenue());
        LOGGER.info("Grid Net Transfer: {}", result.system().getEnergyRevenue());

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
