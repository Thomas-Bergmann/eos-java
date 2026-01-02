package de.hatoka.eos.simulation.capi.business.statistics;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.units.capi.PercentageJsonConverter;

public class HourlyEfficiency
{
    @JsonProperty("hour")
    @JsonPropertyDescription("Hour of the day (0-23)")
    private int hour;
    
    @JsonProperty("efficiency")
    @JsonPropertyDescription("Solar panel efficiency factor as percentage")
    @JsonSerialize(using = PercentageJsonConverter.Serializer.class)
    @JsonDeserialize(using = PercentageJsonConverter.Deserializer.class)
    private Percentage efficiency;

    public int getHour()
    {
        return hour;
    }

    public void setHour(int hour)
    {
        this.hour = hour;
    }

    public Percentage getEfficiency()
    {
        return efficiency;
    }

    public void setEfficiency(Percentage efficiency)
    {
        this.efficiency = efficiency;
    }
}