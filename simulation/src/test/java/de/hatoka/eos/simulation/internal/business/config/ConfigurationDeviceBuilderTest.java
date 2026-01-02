package de.hatoka.eos.simulation.internal.business.config;

import de.hatoka.eos.simulation.capi.business.device.Device;
import de.hatoka.eos.simulation.capi.business.device.DeviceFactory;
import de.hatoka.eos.simulation.capi.business.device.DeviceRef;
import de.hatoka.eos.simulation.capi.business.device.DeviceType;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class ConfigurationDeviceBuilderTest
{
    @Inject
    private DeviceFactory deviceFactory;
    @Inject
    private ConfigurationLoader configurationLoader;

    @Test
    public void testLoadFromResource() throws IOException
    {
        Map<DeviceRef, Device> devices = deviceFactory.createDevices(configurationLoader.load("test-installation-with-car.yaml").getDevices());

        // Test devices
        assertEquals(19, devices.size());

        assertNotNull(devices.get(new DeviceRef(DeviceType.SOLAR_PANEL, "Panel-01")));
        assertNotNull(devices.get(new DeviceRef(DeviceType.GRID, "Grid")));
    }
}