package de.hatoka.eos.devices.capi.business.statistics;

import de.hatoka.eos.units.capi.Percentage;

import java.time.ZonedDateTime;

public interface SolarPanelStatistics
{
    Percentage getEfficiency(ZonedDateTime time);
}