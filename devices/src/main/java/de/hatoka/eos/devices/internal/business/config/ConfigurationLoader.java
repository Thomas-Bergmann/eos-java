package de.hatoka.eos.devices.internal.business.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.hatoka.eos.devices.capi.business.config.InstallationConfig;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class ConfigurationLoader
{
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public InstallationConfig load(String resourcePath) throws IOException
    {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null)
        {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return yamlMapper.readValue(inputStream, InstallationConfig.class);
    }
}