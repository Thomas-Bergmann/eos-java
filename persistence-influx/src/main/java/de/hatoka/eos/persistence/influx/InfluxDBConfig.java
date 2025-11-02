package de.hatoka.eos.persistence.influx;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import jakarta.enterprise.inject.Produces;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Configuration for InfluxDB client.
 */
@Singleton
public class InfluxDBConfig
{
    @ConfigProperty(name = "eos.influxdb.url", defaultValue = "http://localhost:8086")
    String influxdbUrl;

    @ConfigProperty(name = "eos.influxdb.token", defaultValue = "")
    String influxdbToken;

    @ConfigProperty(name = "eos.influxdb.org", defaultValue = "eos")
    String influxdbOrg;

    @ConfigProperty(name = "eos.influxdb.bucket", defaultValue = "forecast")
    String influxdbBucket;

    @Produces
    @Singleton
    public InfluxDBClient influxDBClient()
    {
        return InfluxDBClientFactory.create(influxdbUrl, influxdbToken.toCharArray(), influxdbOrg, influxdbBucket);
    }
}
