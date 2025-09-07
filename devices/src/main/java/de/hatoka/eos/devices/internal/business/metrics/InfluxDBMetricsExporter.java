package de.hatoka.eos.devices.internal.business.metrics;

import com.influxdb.client.DeleteApi;
import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApi;
import com.influxdb.client.domain.DeletePredicateRequest;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;
import de.hatoka.eos.devices.capi.business.device.DeviceRef;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.capi.business.forecast.EnergyPriceForecast;
import de.hatoka.eos.devices.capi.business.forecast.WeatherForecast;
import de.hatoka.eos.devices.capi.business.metrics.ForecastMetricsExporter;
import de.hatoka.eos.devices.capi.business.metrics.SimulationMetricsExporter;
import de.hatoka.eos.devices.capi.business.simulation.SimulationResult;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.Map;

@Singleton
public class InfluxDBMetricsExporter implements SimulationMetricsExporter, ForecastMetricsExporter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(InfluxDBMetricsExporter.class);
    public static final String ENERGY_FLOW = "energy_flow";
    public static final String TAG_SIMULATION = "simulation";

    @ConfigProperty(name = "eos.metrics.influxdb.url", defaultValue = "http://localhost:8086")
    String influxUrl;

    @ConfigProperty(name = "eos.metrics.influxdb.token")
    String influxToken;

    @ConfigProperty(name = "eos.metrics.influxdb.org", defaultValue = "eos")
    String influxOrg;

    @ConfigProperty(name = "eos.metrics.influxdb.bucket", defaultValue = "energy_simulation")
    String influxBucket;

    @ConfigProperty(name = "eos.metrics.influxdb.enabled", defaultValue = "false")
    boolean enabled;

    private InfluxDBClient influxClient;
    private WriteApi writeApi;
    private DeleteApi deleteAPI;

    @PostConstruct
    void initialize()
    {
        if (enabled && influxToken != null)
        {
            try
            {
                influxClient = InfluxDBClientFactory.create(influxUrl, influxToken.toCharArray(), influxOrg, influxBucket);
                writeApi = influxClient.makeWriteApi();
                deleteAPI = influxClient.getDeleteApi();
                LOGGER.info("InfluxDB metrics exporter initialized: {}", influxUrl);
            }
            catch(Exception e)
            {
                LOGGER.warn("Failed to initialize InfluxDB client: {}", e.getMessage());
                enabled = false;
            }
        }
        else
        {
            LOGGER.info("InfluxDB metrics exporter disabled (enabled={}, token present={})", enabled, influxToken != null);
        }
    }

    @PreDestroy
    void cleanup()
    {
        if (writeApi != null)
        {
            writeApi.flush();
            writeApi.close();
            writeApi = null;
        }
        if (influxClient != null)
        {
            influxClient.close();
        }
    }

    /**
     * Delete data from former simulation result
     * @param result former/current simulation result (with same simulation-id, from and to date)
     */
    private void deleteOldData(SimulationResult result)
    {
        // https://docs.influxdata.com/influxdb/v2/write-data/delete-data/
        // '_measurement="example-measurement" AND exampleTag="exampleTagValue"'
        String condition = String.format("_measurement=\"%s\" AND %s=\"%s\"", quotePredicate(ENERGY_FLOW), TAG_SIMULATION,
                        quotePredicate(result.request().simulationId()));
        DeletePredicateRequest predicate = new DeletePredicateRequest()//.predicate(condition)
                                                                       .start(result.step().startDate().toOffsetDateTime())
                                                                       .stop(result.step().endDate().toOffsetDateTime());
        deleteAPI.delete(predicate, influxBucket, influxOrg);
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
                case '\"':
                    sb.append('\\');
                default:
                    sb.append(value.charAt(i));
            }
        }
        return sb.toString();
    }

    /**
     * @return true of influx db is available and writeApi could be created
     */
    boolean isAvailable()
    {
        return enabled && influxClient != null && writeApi != null;
    }

    @Override
    public void exportMetrics(SimulationResult result)
    {
        if (!isAvailable())
        {
            LOGGER.warn("InfluxDB exporter not available, skipping metrics export");
            return;
        }
        deleteOldData(result);
        try
        {
            Instant timestamp = result.step().startDate().toInstant();
            // Export energy flow metrics
            writeApi.writePoint(Point.measurement(ENERGY_FLOW)
                                     .addTag(TAG_SIMULATION, result.request().simulationId())
                                     .addField("produced_kwh", result.system().produced().amount())
                                     .addField("consumed_kwh", -result.system().consumed().amount())
                                     .addField("charged_kwh", -result.system().charged().amount())
                                     .addField("discharged_kwh", +result.system().discharged().amount())
                                     .addField("imported_kwh", result.system().imported().amount())
                                     .addField("exported_kwh", -result.system().exported().amount())
                                     .time(timestamp, WritePrecision.NS));

            // Export cost metrics
            writeApi.writePoint(Point.measurement("energy_costs")
                                     .addTag(TAG_SIMULATION, result.request().simulationId())
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
        writeApi.writePoint(Point.measurement("battery_storage")
                                 .addTag(TAG_SIMULATION, simulationId)
                                 .addTag("device", deviceRef.id())
                                 .addTag("type", deviceRef.type().name())
                                 .addField("storage_percentage", state.percentage().value()) // Convert to percentage
                                 .addField("stored_energy_kwh", state.storedEnergy().amount())
                                 .addField("capacity_kwh", state.maxEnergy().amount())
                                 .time(timestamp, WritePrecision.NS));
    }

    @Override
    public void export(ZonedDateTime time, EnergyPriceForecast energyPriceProvider)
    {
        writeApi.writePoint(Point.measurement("forecast_price")
                                 .addField("import_price_eur", energyPriceProvider.getImportPrice(time).amount())
                                 .addField("export_price_eur", energyPriceProvider.getExportPrice(time).amount())
                                 .time(time.toInstant(), WritePrecision.NS));
    }

    @Override
    public void export(ZonedDateTime time, WeatherForecast weatherService)
    {
        writeApi.writePoint(Point.measurement("forecast_weather")
                                 .addField("sun_probability_percentage", weatherService.getSunProbability(time).value())
                                 .time(time.toInstant(), WritePrecision.NS));
    }
}