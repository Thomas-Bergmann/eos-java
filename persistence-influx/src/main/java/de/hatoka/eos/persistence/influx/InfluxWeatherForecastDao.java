package de.hatoka.eos.persistence.influx;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxTable;
import de.hatoka.eos.persistence.capi.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.WeatherForecastPO;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * InfluxDB implementation for writing forecast data.
 */
@Singleton
public class InfluxWeatherForecastDao implements WeatherForcastDAO
{
    private static final String WEATHER_MEASUREMENT = "weather_forecast";
    public static final String FILTER_INSTANT = """
                    from(bucket: "%s")
                      |> range(start: %s, stop: %s)
                      |> filter(fn: (r) => r["_measurement"] == "%s")
                      |> filter(fn: (r) => r["_field"] == "%s")
                      |> last()
                    """;

    private final WriteApiBlocking writeApi;
    private final DeleteApi deleteApi;
    private final QueryApi queryApi;
    private final String bucketName;
    private final String influxdbOrg;

    @Inject
    InfluxWeatherForecastDao(InfluxDBClient influxDBClient, InfluxDBConfig config)
    {
        writeApi = influxDBClient.getWriteApiBlocking();
        deleteApi = influxDBClient.getDeleteApi();
        queryApi = influxDBClient.getQueryApi();
        bucketName = config.influxdbBucket;
        influxdbOrg = config.influxdbOrg;
    }

    @Override
    public void update(ZonedDateTime time, WeatherForecastPO data)
    {
        try
        {
            Point point = Point.measurement(WEATHER_MEASUREMENT)
                               .time(time.toInstant(), WritePrecision.S)
                               .addField(WeatherForecastPO.COLUMN_SUN_PROBABILITY, data.getSunProbability().value());

            writeApi.writePoint(point);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't write weather forecast to influx", e);
        }
    }

    @Override
    public void delete(ZonedDateTime zonedDateTime)
    {
        try
        {
            // Delete data within a 1-minute window around the specified time
            deleteApi.delete(
                zonedDateTime.minusMinutes(1).toOffsetDateTime(),
                zonedDateTime.plusMinutes(1).toOffsetDateTime(),
                "_measurement=\"" + WEATHER_MEASUREMENT + "\"",
                bucketName,
                influxdbOrg
            );
        }
        catch (Exception e)
        {
            throw new RuntimeException("Can't delete weather forecast from influx", e);
        }
    }

    private String formatTimeForFlux(ZonedDateTime time)
    {
        return time.format(DateTimeFormatter.ISO_INSTANT);
    }

    @Override
    public WeatherForecastPO get(ZonedDateTime zonedDateTime)
    {
        String flux = String.format(FILTER_INSTANT,
                        bucketName,
                        formatTimeForFlux(zonedDateTime.minusMinutes(1)),
                        formatTimeForFlux(zonedDateTime.plusMinutes(1)),
                        WEATHER_MEASUREMENT, WeatherForecastPO.COLUMN_SUN_PROBABILITY);
        List<FluxTable> tables = queryApi.query(flux);
        if (!tables.isEmpty() && !tables.getFirst().getRecords().isEmpty())
        {
            WeatherForecastPO data = new WeatherForecastPO();
            Object value = tables.getFirst().getRecords().getFirst().getValueByKey("_value");
            if (value instanceof Number aNumber)
            {
                data.setSunProbability(new Percentage(aNumber.doubleValue()));
            }
            return data;
        }
        return null;
    }
}
