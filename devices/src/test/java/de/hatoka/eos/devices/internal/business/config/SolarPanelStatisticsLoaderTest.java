package de.hatoka.eos.devices.internal.business.config;

import de.hatoka.eos.devices.capi.business.statistics.HourlyEfficiency;
import de.hatoka.eos.devices.capi.business.statistics.SolarPanelStatisticsConfig;
import de.hatoka.eos.devices.capi.units.Percentage;
import de.hatoka.eos.devices.internal.business.statistics.SolarPanelStatisticsLoader;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class SolarPanelStatisticsLoaderTest
{
    @Test
    public void testLoadFromResource() throws IOException
    {
        // Arrange
        SolarPanelStatisticsLoader loader = new SolarPanelStatisticsLoader();

        // Act
        SolarPanelStatisticsConfig config = loader.load("solar-panel-statistics.yaml");

        // Assert
        assertNotNull(config);
        assertNotNull(config.getHourlyEfficiency());
        assertEquals(11, config.getHourlyEfficiency().size());

        // Test specific efficiency values
        HourlyEfficiency noon = config.getHourlyEfficiency().stream()
            .filter(h -> h.getHour() == 12)
            .findFirst()
            .orElse(null);
        assertNotNull(noon);
        assertEquals(12, noon.getHour());
        assertEquals(new Percentage(1.0), noon.getEfficiency());

        // Test tree shadow scenario (3pm = hour 15)
        HourlyEfficiency treeShadow = config.getHourlyEfficiency().stream()
            .filter(h -> h.getHour() == 15)
            .findFirst()
            .orElse(null);
        assertNotNull(treeShadow);
        assertEquals(15, treeShadow.getHour());
        assertEquals(new Percentage(0.1), treeShadow.getEfficiency());

        // Test heavy shadow (4pm = hour 16)
        HourlyEfficiency heavyShadow = config.getHourlyEfficiency().stream()
            .filter(h -> h.getHour() == 16)
            .findFirst()
            .orElse(null);
        assertNotNull(heavyShadow);
        assertEquals(16, heavyShadow.getHour());
        assertEquals(new Percentage(0.05), heavyShadow.getEfficiency());

        // Test early morning efficiency
        HourlyEfficiency morning = config.getHourlyEfficiency().stream()
            .filter(h -> h.getHour() == 8)
            .findFirst()
            .orElse(null);
        assertNotNull(morning);
        assertEquals(8, morning.getHour());
        assertEquals(new Percentage(0.6), morning.getEfficiency());
    }
}