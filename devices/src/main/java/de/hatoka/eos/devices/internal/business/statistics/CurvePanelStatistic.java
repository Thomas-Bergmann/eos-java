package de.hatoka.eos.devices.internal.business.statistics;

import de.hatoka.eos.devices.capi.business.statistics.SolarPanelStatistics;
import de.hatoka.eos.devices.capi.units.Percentage;

import java.time.ZonedDateTime;

public class CurvePanelStatistic implements SolarPanelStatistics
{
    public static final SolarPanelStatistics CURVED = new CurvePanelStatistic();

    private final int startMinute; // Start time in minutes from midnight
    private final int endMinute;   // End time in minutes from midnight

    public CurvePanelStatistic()
    {
        this(8 * 60, 17 * 60); // 8:00 AM to 5:00 PM
    }

    public CurvePanelStatistic(int startMinute, int endMinute)
    {
        this.startMinute = startMinute;
        this.endMinute = endMinute;
    }

    @Override
    public Percentage getEfficiency(ZonedDateTime time)
    {
        // Convert ZonedDateTime to minutes from midnight
        int currentMinute = time.getHour() * 60 + time.getMinute();

        // Outside sun hours
        if (currentMinute < startMinute || currentMinute >= endMinute)
        {
            return Percentage.ZERO;
        }

        // Calculate position on curve - use a more realistic approach
        double totalRange = endMinute - startMinute;
        double timeFromStart = currentMinute - startMinute;
        double normalizedTime = timeFromStart / totalRange;

        // Use a sine curve that starts at 0, peaks around noon, and ends at 0
        // This creates a more realistic solar panel efficiency curve
        double efficiency = Math.sin(normalizedTime * Math.PI);

        // Apply a scaling factor to make the curve more realistic
        // Solar panels don't reach 100% efficiency, typically max around 80-90%
        double maxEfficiency = 0.85; // 85% max efficiency
        efficiency *= maxEfficiency;

        // Ensure we don't exceed bounds
        return new Percentage(Math.max(0, Math.min(1, efficiency)));
    }
}