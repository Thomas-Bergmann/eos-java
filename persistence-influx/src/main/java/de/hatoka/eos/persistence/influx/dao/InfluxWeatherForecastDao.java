package de.hatoka.eos.persistence.influx.dao;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.NotFoundException;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import de.hatoka.eos.persistence.influx.config.InfluxDBConfig;
import de.hatoka.eos.persistence.capi.weather.WeatherForcastDAO;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastKey;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastPO;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * InfluxDB implementation for writing forecast data.
 */
@Singleton
public class InfluxWeatherForecastDao implements WeatherForcastDAO
{
    private static final String BUCKET = "forecast";
    private static final String WEATHER_MEASUREMENT = "weather_forecast";
    private static final String COLUMN_STATION = "station";
    private static final String COLUMN_SOURCE = "source";
    private static final String GET_QUERY = """
                    from(bucket: "%s")
                      |> range(start: %s, stop: %s)
                      |> filter(fn: (r) => r["_measurement"] == "%s")
                      |> filter(fn: (r) => r["station"] == "%s")
                      |> filter(fn: (r) => r["source"] == "%s")
                      |> filter(fn: (r) => r["_field"] == "%s")
                      |> last()
                    """;
    private static final String DELETE_PREDICATE = """
                    _measurement="%s" AND station="%s" AND source="%s"
                    """;

    private final WriteApiBlocking writeApi;
    private final DeleteApi deleteApi;
    private final QueryApi queryApi;
    private final String influxdbOrg;

    @Inject
    InfluxWeatherForecastDao(InfluxDBConfig config)
    {
        InfluxDBClient influxDBClient = config.getClient(BUCKET);
        writeApi = influxDBClient.getWriteApiBlocking();
        deleteApi = influxDBClient.getDeleteApi();
        queryApi = influxDBClient.getQueryApi();
        influxdbOrg = config.getOrg();
    }

    @Override
    public void update(WeatherForecastKey key, WeatherForecastPO data)
    {
        try
        {
            Point point = Point.measurement(WEATHER_MEASUREMENT)
                               .time(getInstantOf(key.time()), WritePrecision.S)
                               .addTag(COLUMN_STATION, key.station().name())
                               .addTag(COLUMN_SOURCE, key.source().getIdentifier())
                               .addField(WeatherForecastPO.COLUMN_SUN_PROBABILITY, data.getSunProbability().value());

            writeApi.writePoint(point);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't write weather forecast to influx", e);
        }
    }

    Instant getInstantOf(long time)
    {
        return Instant.ofEpochMilli(time);
    }

    ZonedDateTime getZonedOf(long time)
    {
        return getInstantOf(time).atZone(ZoneId.of("UTC"));
    }

    @Override
    public void delete(WeatherForecastKey key)
    {
        try
        {
            // Delete data within a 1-minute window around the specified time
            deleteApi.delete(getZonedOf(key.time()).minusMinutes(1).toOffsetDateTime(), getZonedOf(key.time()).plusMinutes(1).toOffsetDateTime(),
                            DELETE_PREDICATE.formatted(WEATHER_MEASUREMENT, key.station(), key.source().getIdentifier()), BUCKET, influxdbOrg);
        }
        catch(NotFoundException e)
        {
            // ignore if not exists
        }
    }

    private String formatTimeForFlux(ZonedDateTime time)
    {
        return time.format(DateTimeFormatter.ISO_INSTANT);
    }

    @Override
    public WeatherForecastPO get(WeatherForecastKey key)
    {
        String flux = String.format(GET_QUERY, BUCKET, formatTimeForFlux(getZonedOf(key.time()).minusMinutes(1)),
                        formatTimeForFlux(getZonedOf(key.time()).plusMinutes(1)), WEATHER_MEASUREMENT, key.station(), key.source().getIdentifier(), WeatherForecastPO.COLUMN_SUN_PROBABILITY);
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
