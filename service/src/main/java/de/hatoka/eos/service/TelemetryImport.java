package de.hatoka.eos.service;

import de.hatoka.eos.hardware.enphase.usage.EnphaseTelemetry;
import de.hatoka.eos.persistence.capi.usage.UsageDao;
import de.hatoka.eos.persistence.capi.usage.UsageKey;
import de.hatoka.eos.persistence.capi.usage.UsagePO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Singleton
public class TelemetryImport
{
    private static final Logger LOGGER = LoggerFactory.getLogger(TelemetryImport.class);
    @Inject
    private EnphaseTelemetry telemetry;
    @Inject
    private UsageDao usageDao;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd-HH:mm");

    public void run()
    {
        try
        {
            LOGGER.info("Importing telemetry data from today");
            ZonedDateTime time = LocalDateTime.parse("2025/02/12-00:00", FORMATTER).atZone(ZoneId.of("Europe/Berlin"));
            ZonedDateTime endTime = time.plusMonths(3);
            while(time.isBefore(endTime))
            {
                Map<UsageKey, UsagePO> data = telemetry.getDailyUsage(time);
                data.forEach((k, v) -> usageDao.update(k, v));
                LOGGER.debug("Importing telemetry of {} stored", time);
                time = time.plusDays(1);
                // we have only ten requests per minute so wait.
                Thread.sleep(Duration.ofMinutes(1));
            }
        }
        catch(Exception e)
        {
            LoggerFactory.getLogger(getClass()).error("uncatched error", e);
        }
    }
}
