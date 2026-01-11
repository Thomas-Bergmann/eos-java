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
import de.hatoka.eos.persistence.capi.usage.UsageDao;
import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import de.hatoka.eos.persistence.influx.config.InfluxDBConfig;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.EnergyUnits;
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
public class InfluxUsageDao implements UsageDao
{
    private static final String BUCKET = "telemetry";
    private static final String MEASUREMENT = "telemetry_usage";
    private static final String GET_QUERY = """
                    from(bucket: "%s")
                      |> range(start: %s, stop: %s)
                      |> filter(fn: (r) => r["_measurement"] == "%s")
                    """;
    private static final String DELETE_PREDICATE = """
                    _measurement="%s"
                    """;

    private final WriteApiBlocking writeApi;
    private final DeleteApi deleteApi;
    private final QueryApi queryApi;
    private final String influxDbOrg;

    @Inject
    InfluxUsageDao(InfluxDBConfig config)
    {
        InfluxDBClient client = config.getClient(BUCKET);
        writeApi = client.getWriteApiBlocking();
        deleteApi = client.getDeleteApi();
        queryApi = client.getQueryApi();
        influxDbOrg = config.getOrg();
    }

    @Override
    public void update(UsageKey key, UsagePO data)
    {
        try
        {
            Point point = Point.measurement(MEASUREMENT)
                               .time(key.getInstant(), WritePrecision.S)
                               .addField(UsagePO.COLUMN_CONSUMPTION, data.getConsumption().convert(EnergyUnits.WH).amount())
                               .addField(UsagePO.COLUMN_PRODUCTION, data.getProduction().convert(EnergyUnits.WH).amount())
                               .addField(UsagePO.COLUMN_IMPORT, data.getGridImport().convert(EnergyUnits.WH).amount())
                               .addField(UsagePO.COLUMN_EXPORT, data.getGridExport().convert(EnergyUnits.WH).amount())
                               .addField(UsagePO.COLUMN_CHARGE, data.getCharge().convert(EnergyUnits.WH).amount())
                               .addField(UsagePO.COLUMN_DISCHARGE, data.getDischarge().convert(EnergyUnits.WH).amount())
                               .addField(UsagePO.COLUMN_BATTERY_STATE, data.getBatteryState().value());
            writeApi.writePoint(point);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Can't write weather forecast to influx", e);
        }
    }

    @Override
    public void delete(UsageKey key)
    {
        try
        {
            // Delete data within a 1-minute window around the specified time
            deleteApi.delete(key.getZonedDateTime().minusMinutes(1).toOffsetDateTime(), key.getZonedDateTime().plusMinutes(1).toOffsetDateTime(),
                            DELETE_PREDICATE.formatted(MEASUREMENT), BUCKET, influxDbOrg);
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
    public UsagePO get(UsageKey key)
    {
        UsagePO result = new UsagePO();
        String flux = String.format(GET_QUERY, BUCKET, formatTimeForFlux(key.getZonedDateTime().minusMinutes(1)),
                        formatTimeForFlux(key.getZonedDateTime().plusMinutes(1)), MEASUREMENT);
        // Aggregate all fields for the same timestamp
        List<FluxTable> tables = queryApi.query(flux);
        for (FluxTable table : tables)
        {
            for (FluxRecord record : table.getRecords())
            {
                String field = (String)record.getValueByKey("_field");
                Object value = record.getValueByKey("_value");
                setValue(result, field, value);
            }
        }
        return result;
    }

    private void setValue(UsagePO result, String field, Object value)
    {
        switch(field)
        {
            case UsagePO.COLUMN_CONSUMPTION:
                result.setConsumption(getEnergyValue(UsagePO.COLUMN_CONSUMPTION, value));
                break;
            case UsagePO.COLUMN_PRODUCTION:
                result.setProduction(getEnergyValue(UsagePO.COLUMN_PRODUCTION, value));
                break;
            case UsagePO.COLUMN_IMPORT:
                result.setGridImport(getEnergyValue(UsagePO.COLUMN_IMPORT, value));
                break;
            case UsagePO.COLUMN_EXPORT:
                result.setGridExport(getEnergyValue(UsagePO.COLUMN_EXPORT, value));
                break;
            case UsagePO.COLUMN_CHARGE:
                result.setCharge(getEnergyValue(UsagePO.COLUMN_CHARGE, value));
                break;
            case UsagePO.COLUMN_DISCHARGE:
                result.setDischarge(getEnergyValue(UsagePO.COLUMN_DISCHARGE, value));
                break;
            case UsagePO.COLUMN_BATTERY_STATE:
                result.setBatteryState(getPercentageValue(UsagePO.COLUMN_BATTERY_STATE, value));
                break;
        }
    }

    private Energy getEnergyValue(String key, Object stored)
    {
        if (stored == null)
        {
            return Energy.ZERO; // or null, depending on your requirements
        }
        if (stored instanceof Number n)
        {
            return Energy.ofWh(n.doubleValue());
        }
        throw new IllegalStateException("Can't resolve " + key + " with value " + stored);
    }

    private Percentage getPercentageValue(String key, Object stored)
    {
        if (stored == null)
        {
            return Percentage.ZERO; // or null, depending on your requirements
        }
        if (stored instanceof Number n)
        {
            return Percentage.valueOf(n.doubleValue());
        }
        throw new IllegalStateException("Can't resolve " + key + " with value " + stored);
    }
}
