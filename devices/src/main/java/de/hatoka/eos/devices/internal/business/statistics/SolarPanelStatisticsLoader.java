package de.hatoka.eos.devices.internal.business.statistics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.hatoka.eos.devices.capi.business.statistics.SolarPanelStatisticsConfig;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class SolarPanelStatisticsLoader
{
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public SolarPanelStatisticsConfig load(String resourcePath) throws IOException
    {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null)
        {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return yamlMapper.readValue(inputStream, SolarPanelStatisticsConfig.class);
    }
}