package de.hatoka.eos.simulation.internal.business.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import de.hatoka.eos.simulation.capi.business.config.InstallationConfig;
import de.hatoka.eos.simulation.capi.business.config.SimulationConfig;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class ConfigurationLoader
{
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory())
            .registerModule(new JavaTimeModule());

    @Deprecated
    public InstallationConfig load(String resourcePath) throws IOException
    {
        return loadInstallation(resourcePath);
    }

    public InstallationConfig loadInstallation(String resourcePath) throws IOException
    {
        return load(resourcePath, InstallationConfig.class);
    }

    public SimulationConfig loadSimulation(String resourcePath) throws IOException
    {
        return load(resourcePath, SimulationConfig.class);
    }

    public <T> T load(String resourcePath, Class<T> classOfConfig) throws IOException
    {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null)
        {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return yamlMapper.readValue(inputStream, classOfConfig);
    }
}