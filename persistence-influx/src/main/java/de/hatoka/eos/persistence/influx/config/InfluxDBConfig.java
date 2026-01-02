package de.hatoka.eos.persistence.influx.config;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Configuration for InfluxDB client.
 */
@Singleton
public class InfluxDBConfig
{
    @ConfigProperty(name = "eos.influxdb.url", defaultValue = "http://localhost:8086")
    private String influxUrl;

    @ConfigProperty(name = "eos.influxdb.token", defaultValue = "")
    private String influxToken;

    @ConfigProperty(name = "eos.influxdb.org", defaultValue = "eos")
    private String influxOrg;

    public InfluxDBClient getClient(String bucket)
    {
        return InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, bucket);
    }

    public String getOrg()
    {
        return influxOrg;
    }
}
