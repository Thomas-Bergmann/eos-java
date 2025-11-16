package de.hatoka.eos.persistence.influx;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.QueryApi;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.query.FluxRecord;
import com.influxdb.query.FluxTable;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockDao;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockKey;
import de.hatoka.eos.persistence.capi.energystock.EnergyStockPO;
import de.hatoka.eos.persistence.capi.weather.WeatherForecastPO;
import de.hatoka.eos.units.capi.Money;
import de.hatoka.eos.units.capi.Percentage;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

/**
 * InfluxDB implementation for writing forecast data.
 */
@Singleton
public class InfluxEnergyStockDao implements EnergyStockDao
{
    private static final String MEASUREMENT = "energystock_forecast";
    public static final String GET_QUERY = """
                    from(bucket: "%s")
                      |> range(start: %s, stop: %s)
                      |> filter(fn: (r) => r["_measurement"] == "%s")
                      |> filter(fn: (r) => r["_field"] == "%s")
                      |> last()
                    """;
    public static final String DELETE_PREDICATE = """
                    _measurement="%s"
                    """;

    private final WriteApiBlocking writeApi;
    private final DeleteApi deleteApi;
    private final QueryApi queryApi;
    private final String bucketName;
    private final String influxdbOrg;

    @Inject
    InfluxEnergyStockDao(InfluxDBClient influxDBClient, InfluxDBConfig config)
    {
        writeApi = influxDBClient.getWriteApiBlocking();
        deleteApi = influxDBClient.getDeleteApi();
        queryApi = influxDBClient.getQueryApi();
        bucketName = config.influxdbBucket;
        influxdbOrg = config.influxdbOrg;
    }

    @Override
    public void update(EnergyStockKey key, EnergyStockPO data)
    {
        try
        {
            Point point = Point.measurement(MEASUREMENT)
                               .time(key.time().toInstant(), WritePrecision.S)
                               .addTag("currency", data.getDayAheadPrice().currencyMnemonic())
                               .addField(EnergyStockPO.COLUMN_DAY_AHEAD, data.getDayAheadPrice().amount().doubleValue());

            writeApi.writePoint(point);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't write weather forecast to influx", e);
        }
    }

    @Override
    public void delete(EnergyStockKey key)
    {
        try
        {
            // Delete data within a 1-minute window around the specified time
            deleteApi.delete(key.time().minusMinutes(1).toOffsetDateTime(), key.time().plusMinutes(1).toOffsetDateTime(),
                            DELETE_PREDICATE.formatted(MEASUREMENT), bucketName, influxdbOrg);
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
    public EnergyStockPO get(EnergyStockKey key)
    {
        String flux = String.format(GET_QUERY, bucketName, formatTimeForFlux(key.time().minusMinutes(1)),
                        formatTimeForFlux(key.time().plusMinutes(1)), MEASUREMENT, EnergyStockPO.COLUMN_DAY_AHEAD);
        for (FluxTable table : queryApi.query(flux))
        {
            for (FluxRecord record : table.getRecords())
            {
                EnergyStockPO data = convert(record);
                if (data != null)
                {
                    return data;
                }
            }
        }
        return null;
    }

    private EnergyStockPO convert(FluxRecord record)
    {
        EnergyStockPO data = new EnergyStockPO();
        Object value = record.getValueByKey("_value");
        Object currencyValue= record.getValueByKey("currency");
        if (value instanceof Number aNumber && currencyValue instanceof String currency)
        {
            data.setDayAheadPrice(new Money(BigDecimal.valueOf(aNumber.doubleValue()), currency));
        }
        else
        {
            return null;
        }
        return data;
    }
}
