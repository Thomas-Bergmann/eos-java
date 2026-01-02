package de.hatoka.eos.persistence.influx.config;

import com.influxdb.client.BucketsApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.domain.Bucket;
import com.influxdb.client.domain.Organization;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Configuration for InfluxDB client.
 * Ensures required buckets exist on startup.
 */
@Singleton
public class InfluxDBConfig
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBConfig.class);

    /**
     * created or existing buckets
     */
    private static final Map<String, Bucket> ENSURED_BUCKETS = new ConcurrentHashMap<>();

    @ConfigProperty(name = "eos.influxdb.url", defaultValue = "http://localhost:8086")
    private String influxUrl;

    @ConfigProperty(name = "eos.influxdb.token", defaultValue = "")
    private String influxToken;

    @ConfigProperty(name = "eos.influxdb.org", defaultValue = "eos")
    private String influxOrg;

    /**
     * @param bucket bucket name
     * @return client for bucket
     */
    public InfluxDBClient getClient(String bucket)
    {
        return InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, ENSURED_BUCKETS.computeIfAbsent(bucket, this::ensureBucketExists).getName());
    }

    public String getOrg()
    {
        return influxOrg;
    }

    /**
     * Ensures that a bucket exists in InfluxDB, creating it if necessary.
     * This method is idempotent and thread-safe.
     *
     * @param bucketName the name of the bucket to ensure exists
     */
    private Bucket ensureBucketExists(String bucketName)
    {
        try (InfluxDBClient client = InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg))
        {
            BucketsApi bucketsApi = client.getBucketsApi();

            // Check if bucket exists
            Bucket existingBucket = bucketsApi.findBucketByName(bucketName);
            if (existingBucket != null)
            {
                return existingBucket;
            }
            // Create the bucket
            Organization org = client.getOrganizationsApi()
                                     .findOrganizations()
                                     .stream()
                                     .filter(o -> o.getName().equals(influxOrg))
                                     .findFirst()
                                     .orElseThrow(() -> new RuntimeException("Organization not found: " + influxOrg));

            return bucketsApi.createBucket(bucketName, org);
        }
        catch (Exception e)
        {
            LOGGER.warn("Could not ensure InfluxDB bucket '{}' exists. You may need to create it manually: influx bucket create -n {} -o {} -r 0",
                    bucketName, bucketName, influxOrg);
            throw e;
        }
    }
}
