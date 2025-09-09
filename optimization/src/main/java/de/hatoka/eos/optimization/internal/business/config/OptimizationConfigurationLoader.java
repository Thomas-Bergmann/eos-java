package de.hatoka.eos.optimization.internal.business.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import de.hatoka.eos.optimization.capi.goals.OptimizationGoals;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.InputStream;

@Singleton
public class OptimizationConfigurationLoader
{
    private final ObjectMapper yamlMapper = new ObjectMapper(new YAMLFactory());

    public OptimizationGoals load(String resourcePath) throws IOException
    {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourcePath);
        if (inputStream == null)
        {
            throw new IOException("Resource not found: " + resourcePath);
        }
        return yamlMapper.readValue(inputStream, OptimizationGoals.class);
    }
}
