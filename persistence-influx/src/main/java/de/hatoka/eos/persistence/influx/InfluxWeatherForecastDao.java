package de.hatoka.eos.persistence.influx;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import de.hatoka.eos.persistence.capi.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.WeatherForecastPO;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * InfluxDB implementation for writing forecast data.
 */
@Singleton
public class InfluxWeatherForecastDao implements WeatherForcastDAO
{
    private static final String WEATHER_MEASUREMENT = "weather_forecast";
    private static final String COLUMN_STATION = "station";
    public static final String GET_QUERY = """
                    from(bucket: "%s")
                      |> range(start: %s, stop: %s)
                      |> filter(fn: (r) => r["_measurement"] == "%s")
                      |> filter(fn: (r) => r["station"] == "%s")
                      |> filter(fn: (r) => r["_field"] == "%s")
                      |> last()
                    """;
    public static final String DELETE_PREDICATE = """
                    _measurement="%s" AND station="%s"
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
    public void update(WeatherForecastKey key, WeatherForecastPO data)
    {
        try
        {
            Point point = Point.measurement(WEATHER_MEASUREMENT)
                               .time(key.time().toInstant(), WritePrecision.S)
                               .addTag(COLUMN_STATION, key.station())
                               .addField(WeatherForecastPO.COLUMN_SUN_PROBABILITY, data.getSunProbability().value());

            writeApi.writePoint(point);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't write weather forecast to influx", e);
        }
    }

    @Override
    public void delete(WeatherForecastKey key)
    {
        try
        {
            // Delete data within a 1-minute window around the specified time
            deleteApi.delete(key.time().minusMinutes(1).toOffsetDateTime(), key.time().plusMinutes(1).toOffsetDateTime(),
                            DELETE_PREDICATE.formatted(WEATHER_MEASUREMENT, key.station()), bucketName, influxdbOrg);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't delete weather forecast from influx", e);
        }
    }

    private String formatTimeForFlux(ZonedDateTime time)
    {
        return time.format(DateTimeFormatter.ISO_INSTANT);
    }

    @Override
    public WeatherForecastPO get(WeatherForecastKey key)
    {
        String flux = String.format(GET_QUERY, bucketName, formatTimeForFlux(key.time().minusMinutes(1)),
                        formatTimeForFlux(key.time().plusMinutes(1)), WEATHER_MEASUREMENT, key.station(), WeatherForecastPO.COLUMN_SUN_PROBABILITY);
        for (FluxTable table : queryApi.query(flux))
        {
            for (FluxRecord record : table.getRecords())
            {
                WeatherForecastPO data = convert(record);
                if (data != null)
                {
                    return data;
                }
            }
        }
        return null;
    }

    private WeatherForecastPO convert(FluxRecord record)
    {
        WeatherForecastPO data = new WeatherForecastPO();
        Object value = record.getValueByKey("_value");
        if (value instanceof Number aNumber)
        {
            data.setSunProbability(new Percentage(aNumber.doubleValue()));
        }
        else
        {
            return null;
        }
        return data;
    }
}
