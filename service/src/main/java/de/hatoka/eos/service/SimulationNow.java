package de.hatoka.eos.service;

import de.hatoka.eos.simulation.capi.business.config.InstallationConfig;
import de.hatoka.eos.simulation.capi.business.device.DeviceFactory;
import de.hatoka.eos.simulation.capi.business.forecast.Forecasts;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationRequest;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import de.hatoka.eos.simulation.capi.business.simulation.Simulator;
import de.hatoka.eos.simulation.internal.business.config.ConfigurationLoader;
import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;

@Singleton
public class SimulationNow
{
    @Inject
    private DeviceFactory deviceFactory;
    @Inject
    private ConfigurationLoader configurationLoader;
    @Inject
    private Simulator simulator;

    private static final Logger LOGGER = LoggerFactory.getLogger(SimulationNow.class);

    public void run() throws Exception
    {
        LOGGER.info("Starting simulation...");
        // Arrange - Load actual test-installation.yaml configuration
        InstallationConfig config = configurationLoader.loadInstallation("installation-without-car.yaml");

        // Simulate one full day (24 hours) with 15,-hour steps
        ZonedDateTime startDate = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("UTC"));
        ZonedDateTime endDate = startDate.plusDays(1);
        Duration stepDuration = Duration.ofMinutes(15);

        SimulationRequest request = new SimulationRequest("today-energy-simulation", startDate, endDate, stepDuration, deviceFactory.createDevices(config.getDevices()), Collections.emptyMap(), Forecasts.STANDARD);
        SimulationResult result = simulator.simulate(request);
        LOGGER.info("Simulation finished with revenue {}", result.system().getEnergyRevenue());
    }
}