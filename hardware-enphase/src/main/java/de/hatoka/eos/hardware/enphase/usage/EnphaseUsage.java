package de.hatoka.eos.hardware.enphase.usage;

import de.hatoka.eos.hardware.enphase.auth.EnphaseAuthentication;
import de.hatoka.eos.hardware.enphase.clients.EnphaseSiteLevelProductionMonitoringClient;
import de.hatoka.eos.hardware.enphase.config.EnphaseConfiguration;
import de.hatoka.eos.hardware.enphase.model.ConsumptionResponseRO;
import de.hatoka.eos.hardware.enphase.model.ProductionResponseRO;
import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import de.hatoka.eos.units.capi.Energy;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Singleton
public class EnphaseUsage
{
    @Inject
    private EnphaseAuthentication authentication;
    @Inject
    private EnphaseConfiguration configuration;

    @Inject
    @RestClient
    private EnphaseSiteLevelProductionMonitoringClient restClient;

    private String format(ZonedDateTime date)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        return df.format(new Date(date.toInstant().toEpochMilli()));
    }

    Map<UsageKey, UsagePO> getDailyUsage(ZonedDateTime from, ZonedDateTime to)
    {
        Map<UsageKey, UsagePO> result = new HashMap<>();
        readConsumption(from, to, result);
        readProduction(from, to, result);
        return result;
    }

    private void readConsumption(ZonedDateTime from, ZonedDateTime to, Map<UsageKey, UsagePO> result)
    {
        ConsumptionResponseRO response = restClient.getConsumption(authentication.getAuthorization(), configuration.getSystemId(),
                        configuration.getApiKey(), format(from), format(to));
        ZonedDateTime thisDate = from;
        for(int e : response.getConsumption())
        {
            result.computeIfAbsent(UsageKey.valueOf(thisDate), k -> new UsagePO()).setConsumption(Energy.ofWh(e));
            thisDate = thisDate.plusDays(1);
        }
    }

    private void readProduction(ZonedDateTime from, ZonedDateTime to, Map<UsageKey, UsagePO> result)
    {
        ProductionResponseRO response = restClient.getProduction(authentication.getAuthorization(), configuration.getSystemId(),
                        configuration.getApiKey(), format(from), format(to));
        ZonedDateTime thisDate = from;
        for(int e : response.getProduction())
        {
            result.computeIfAbsent(UsageKey.valueOf(thisDate), k -> new UsagePO()).setProduction(Energy.ofWh(e));
            thisDate = thisDate.plusDays(1);
        }
    }
}
