package de.hatoka.eos.hardware.enphase.usage;

import de.hatoka.eos.hardware.enphase.auth.EnphaseAuthentication;
import de.hatoka.eos.hardware.enphase.clients.EnphaseSiteLevelProductionMonitoringClient;
import de.hatoka.eos.hardware.enphase.clients.Granularity;
import de.hatoka.eos.hardware.enphase.config.EnphaseConfiguration;
import de.hatoka.eos.hardware.enphase.model.*;
import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import de.hatoka.eos.units.capi.Energy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class EnphaseTelemetry
{
    private static final int MINUTES_ADJUSTMENT = 15;
    @Inject
    private EnphaseAuthentication authentication;
    @Inject
    private EnphaseConfiguration configuration;

    @Inject
    @RestClient
    private EnphaseSiteLevelProductionMonitoringClient restClient;

    private long asEpochSeconds(ZonedDateTime time)
    {
        return time.toInstant().getEpochSecond();
    }

    public Map<UsageKey, UsagePO> getDailyUsage(ZonedDateTime day)
    {
        ZonedDateTime from = day.withHour(0).withMinute(0).withSecond(0).withNano(0);
        ZonedDateTime to = from.plusDays(1);
        // make sure that first element is midnight (api excludes the explicit from date)
        Map<UsageKey, UsagePO> result = new HashMap<>();
        readProduction(from, to, result);
        readConsumption(from, to, result);
        readImport(from, to, result);
        readExport(from, to, result);
        readBattery(from, to, result);
        fixProduction(result);
        return result;
    }

    /**
     * Some hardware of production consumes energy, these energy is counted at production and consumption
     * @param result fixed production (if there is a difference less than 5 Wh
     */
    private void fixProduction(Map<UsageKey, UsagePO> result)
    {
        result.forEach((k, value) -> {
            Energy positive = value.getProduction().add(value.getGridImport()).add(value.getDischarge());
            Energy negative = value.getConsumption().add(value.getGridExport()).add(value.getCharge());
            Energy difference = positive.add(negative);
            if (!difference.isZero() && difference.absolute().lessThan(Energy.ofWh(5)))
            {
                value.setProduction(value.getProduction().subtract(difference));
            }
        });
    }

    private void readConsumption(ZonedDateTime from, ZonedDateTime to, Map<UsageKey, UsagePO> result)
    {
        TelemetryConsumptionResponseRO response = restClient.getConsumptionTelemetry(authentication.getAuthorization(), configuration.getSystemId(),
                        configuration.getApiKey(), asEpochSeconds(from.minusMinutes(MINUTES_ADJUSTMENT)), asEpochSeconds(to), Granularity.DAY.getQueryParameter());
        for(Map.Entry<Date, Energy> e : response.getConsumption().entrySet())
        {
            result.computeIfAbsent(UsageKey.valueOf(e.getKey()), k -> new UsagePO()).setConsumption(e.getValue().negate());
        }
    }

    private void readProduction(ZonedDateTime from, ZonedDateTime to, Map<UsageKey, UsagePO> result)
    {
        TelemetryProductionResponseRO response = restClient.getProductionTelemetry(authentication.getAuthorization(), configuration.getSystemId(),
                        configuration.getApiKey(), asEpochSeconds(from.minusMinutes(MINUTES_ADJUSTMENT)), asEpochSeconds(to),
                        Granularity.DAY.getQueryParameter());
        for (Map.Entry<Date, Energy> e : response.getTelemetry().entrySet())
        {
            result.computeIfAbsent(UsageKey.valueOf(e.getKey()), k -> new UsagePO()).setProduction(e.getValue());
        }
    }

    private void readImport(ZonedDateTime from, ZonedDateTime to, Map<UsageKey, UsagePO> result)
    {
        TelemetryImportResponseRO response = restClient.getImportTelemetry(authentication.getAuthorization(), configuration.getSystemId(),
                        configuration.getApiKey(), asEpochSeconds(from.minusMinutes(MINUTES_ADJUSTMENT)), asEpochSeconds(to),
                        Granularity.DAY.getQueryParameter());
        for (Map.Entry<Date, Energy> e : response.getTelemetry().entrySet())
        {
            result.computeIfAbsent(UsageKey.valueOf(e.getKey()), k -> new UsagePO()).setGridImport(e.getValue());
        }
    }

    private void readExport(ZonedDateTime from, ZonedDateTime to, Map<UsageKey, UsagePO> result)
    {
        TelemetryExportResponseRO response = restClient.getExportTelemetry(authentication.getAuthorization(), configuration.getSystemId(),
                        configuration.getApiKey(), asEpochSeconds(from.minusMinutes(MINUTES_ADJUSTMENT)), asEpochSeconds(to),
                        Granularity.DAY.getQueryParameter());
        for (Map.Entry<Date, Energy> e : response.getTelemetry().entrySet())
        {
            result.computeIfAbsent(UsageKey.valueOf(e.getKey()), k -> new UsagePO()).setGridExport(e.getValue().negate());
        }
    }

    private void readBattery(ZonedDateTime from, ZonedDateTime to, Map<UsageKey, UsagePO> result)
    {
        TelemetryBatteryResponseRO response = restClient.getBatteryTelemetry(authentication.getAuthorization(), configuration.getSystemId(),
                        configuration.getApiKey(), asEpochSeconds(from.minusMinutes(MINUTES_ADJUSTMENT)), asEpochSeconds(to),
                        Granularity.DAY.getQueryParameter());
        for (Map.Entry<Date, TelemetryBatteryResponseRO.BatteryInterval> e : response.getTelemetry().entrySet())
        {
            UsagePO usage = result.computeIfAbsent(UsageKey.valueOf(e.getKey()), k -> new UsagePO());
            usage.setBatteryState(e.getValue().percentage());
            usage.setCharge(e.getValue().charge().negate());
            usage.setDischarge(e.getValue().discharge());
        }
    }
}
