package de.hatoka.eos.metrics.influx;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import com.influxdb.exceptions.NotFoundException;
import de.hatoka.eos.persistence.influx.config.InfluxDBConfig;
import de.hatoka.eos.simulation.capi.business.device.DeviceRef;
import de.hatoka.eos.simulation.capi.business.device.DeviceState;
import de.hatoka.eos.simulation.capi.business.device.DeviceType;
import de.hatoka.eos.simulation.capi.business.metrics.SimulationMetricsExporter;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationResult;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;

@Singleton
public class InfluxDBMetricsExporter implements SimulationMetricsExporter
{
    private static final String BUCKET = "metrics";
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBMetricsExporter.class);
    private static final String MEASUREMENT = "energy_flow";
    private static final String TAG_SIMULATION = "simulation";

    private final WriteApi writeApi;
    private final DeleteApi deleteApi;
    private final String influxDbOrg;

    @Inject
    InfluxDBMetricsExporter(InfluxDBConfig config)
    {
        InfluxDBClient client = config.getClient(BUCKET);
        writeApi = client.makeWriteApi();
        deleteApi = client.getDeleteApi();
        influxDbOrg = config.getOrg();
    }
    /**
     * Delete data from former simulation result
     * @param result former/current simulation result (with same simulation-id, from and to date)
     */
    private void deleteOldData(SimulationResult result)
    {
        // https://docs.influxdata.com/influxdb/v2/write-data/delete-data/
        // '_measurement="example-measurement" AND exampleTag="exampleTagValue"'
        String condition = String.format("_measurement=\"%s\" AND %s=\"%s\"", quotePredicate(MEASUREMENT), TAG_SIMULATION,
                        quotePredicate(result.request().simulationId()));
        DeletePredicateRequest predicate = new DeletePredicateRequest()//.predicate(condition)
                                                                       .start(result.step().startDate().toOffsetDateTime())
                                                                       .stop(result.step().endDate().toOffsetDateTime());
        try
        {
            deleteApi.delete(predicate, BUCKET, influxDbOrg);
        }
        catch(NotFoundException e)
        {
            // ignore if old data or bucket not found
        }
    }

    /**
     * Copy from com.influxdb.client.write.Point#escapeValue(StringBuilder, String)
     * @param value parameter value (e.g. for predicate)
     * @return encoded value
     */
    private String quotePredicate(String value)
    {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            switch (value.charAt(i)) {
                case '\\':
                case '"':
                    sb.append('\\');
                default:
                    sb.append(value.charAt(i));
            }
        }
        return sb.toString();
    }

    @Override
    public void exportMetrics(SimulationResult result)
    {
        deleteOldData(result);
        try
        {
            Instant timestamp = result.step().startDate().toInstant();
            // Export energy flow metrics
            writeApi.writePoint(Point.measurement(MEASUREMENT)
                                     .addTag(TAG_SIMULATION, result.request().simulationId())
                                     .addTag("kind", "flow")
                                     .addField("produced_kwh", result.system().produced().amount())
                                     .addField("consumed_kwh", -result.system().consumed().amount())
                                     .addField("charged_kwh", -result.system().charged().amount())
                                     .addField("discharged_kwh", +result.system().discharged().amount())
                                     .addField("imported_kwh", result.system().imported().amount())
                                     .addField("exported_kwh", -result.system().exported().amount())
                                     .time(timestamp, WritePrecision.NS));

            // Export cost metrics
            writeApi.writePoint(Point.measurement(MEASUREMENT)
                                     .addTag(TAG_SIMULATION, result.request().simulationId())
                                     .addTag("kind", "costs")
                                     .addField("grid_import_eur", result.system().importRevenue().amount().doubleValue())
                                     .addField("grid_export_eur", result.system().exportRevenue().amount().doubleValue())
                                     .addField("grid_net_eur", result.system().getEnergyRevenue().amount().doubleValue())
                                     .time(timestamp, WritePrecision.NS));

            // Export battery storage metrics
            // overall battery state
            for (Map.Entry<DeviceRef, DeviceState> batteryEntry : result.endState()
                                                                        .entrySet()
                                                                        .stream()
                                                                        .filter((e) -> e.getKey().type().equals(DeviceType.BATTERY))
                                                                        .toList())
            {
                writeBattery(result.request().simulationId(), batteryEntry.getKey(), batteryEntry.getValue(), timestamp);
            }
            for (Map.Entry<DeviceRef, DeviceState> carEntry : result.endState()
                                                                    .entrySet()
                                                                    .stream()
                                                                    .filter((e) -> e.getKey().type().equals(DeviceType.ELECTRIC_CAR))
                                                                    .toList())
            {
                writeBattery(result.request().simulationId(), carEntry.getKey(), carEntry.getValue(), timestamp);
            }
            LOGGER.trace("Exported simulation metrics with battery data for: {} at {}", result.request().simulationId(), result.request().endDate());
        }
        catch(Exception e)
        {
            LOGGER.error("Failed to export metrics to InfluxDB. simulation: {}", result.request().simulationId(), e);
        }
    }

    /**
     * Write storage/capacity of battery to influx db.
     * @param simulationId simulation identifier
     * @param deviceRef battery identifier
     * @param state state of battery
     * @param timestamp time of measurement
     */
    private void writeBattery(String simulationId, DeviceRef deviceRef, DeviceState state, Instant timestamp)
    {
        writeApi.writePoint(Point.measurement(MEASUREMENT)
                                 .addTag(TAG_SIMULATION, simulationId)
                                 .addTag("kind", "device")
                                 .addTag("device", deviceRef.id())
                                 .addTag("type", deviceRef.type().name())
                                 .addField("storage_percentage", state.percentage().value()) // Convert to percentage
                                 .addField("stored_energy_kwh", state.storedEnergy().amount())
                                 .addField("capacity_kwh", state.maxEnergy().amount())
                                 .time(timestamp, WritePrecision.NS));
    }
}
