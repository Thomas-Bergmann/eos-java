package de.hatoka.eos.devices.internal.business.statistics;

import de.hatoka.eos.devices.capi.business.statistics.HourlyEfficiency;
import de.hatoka.eos.devices.capi.business.statistics.SolarPanelStatisticsConfig;
import de.hatoka.eos.devices.capi.business.statistics.SolarPanelStatistics;
import de.hatoka.eos.devices.capi.units.Percentage;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SolarPanelStatisticsImpl implements SolarPanelStatistics
{
    public static final SolarPanelStatistics FULL_EFFICIENCY = new SolarPanelStatisticsImpl(createDefaultFullEfficiencyMap());

    private static Map<Integer, Percentage> createDefaultFullEfficiencyMap()
    {
        Map<Integer, Percentage> defaultMap = new HashMap<>();
        for (int hour = 0; hour < 24; hour++)
        {
            defaultMap.put(hour, new Percentage(1.0));
        }
        return defaultMap;
    }


    public static SolarPanelStatistics create(SolarPanelStatisticsConfig config)
    {
        return new SolarPanelStatisticsImpl(config.getHourlyEfficiency()
                                                  .stream()
                                                  .collect(Collectors.toMap(HourlyEfficiency::getHour,
                                                                  HourlyEfficiency::getEfficiency)));
    }

    private final Map<Integer, Percentage> hourlyEfficiency;

    public SolarPanelStatisticsImpl(Map<Integer, Percentage> hourlyEfficiency)
    {
        this.hourlyEfficiency = hourlyEfficiency;
    }

    @Override
    public Percentage getEfficiency(ZonedDateTime time)
    {
        int hour = time.getHour();
        return hourlyEfficiency.getOrDefault(hour, Percentage.ZERO);
    }
}