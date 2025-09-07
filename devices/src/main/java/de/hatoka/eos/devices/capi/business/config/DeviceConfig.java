package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import de.hatoka.eos.devices.capi.business.device.DeviceType;
import de.hatoka.eos.devices.capi.business.statistics.SolarPanelStatistics;
import de.hatoka.eos.devices.capi.units.Energy;
import de.hatoka.eos.devices.capi.units.Percentage;
import de.hatoka.eos.devices.capi.units.PercentageJsonConverter;
import de.hatoka.eos.devices.capi.units.Power;
import de.hatoka.eos.devices.internal.business.statistics.CurvePanelStatistic;
import de.hatoka.eos.devices.internal.business.statistics.SolarPanelStatisticsImpl;
import de.hatoka.eos.devices.internal.business.statistics.SolarPanelStatisticsLoader;

import java.io.IOException;

public class DeviceConfig
{
    @JsonProperty("type")
    @JsonPropertyDescription("Type of the device")
    private DeviceType type;
    
    @JsonProperty("name")
    @JsonPropertyDescription("Identifier or name of the device")
    private String name;
    
    @JsonProperty("production")
    @JsonPropertyDescription("Power generation/production of device")
    private Power production;
    
    @JsonProperty("capacity")
    @JsonPropertyDescription("Energy storage capacity of the device (battery capacity)")
    private Energy capacity;
    
    @JsonProperty("chargeRate")
    @JsonPropertyDescription("Maximum charging power rate for batteries")
    private Power chargeRate;
    
    @JsonProperty("dischargeRate")
    @JsonPropertyDescription("Maximum discharging power rate for batteries")
    private Power dischargeRate;
    
    @JsonProperty("consumption")
    @JsonPropertyDescription("Power consumption rate for consuming devices")
    private Power consumption;
    
    @JsonProperty("count")
    @JsonPropertyDescription("Number of identical devices to create (default: 1)")
    private int count = 1;
    
    @JsonProperty("statisticsResource")
    @JsonPropertyDescription("Path to statistics resource for this device (solar panels only)")
    private String statisticsResource;
    
    @JsonProperty("chargingEfficiency")
    @JsonPropertyDescription("Charging efficiency (in percentage); default: 100%")
    @JsonSerialize(using = PercentageJsonConverter.Serializer.class)
    @JsonDeserialize(using = PercentageJsonConverter.Deserializer.class)
    private Percentage chargingEfficiency = new Percentage(0.90); // Default to 90% efficiency

    @JsonProperty("dischargingEfficiency")
    @JsonPropertyDescription("Discharging efficiency (in percentage); default: 100%")
    @JsonSerialize(using = PercentageJsonConverter.Serializer.class)
    @JsonDeserialize(using = PercentageJsonConverter.Deserializer.class)
    private Percentage dischargingEfficiency = new Percentage(0.90); // Default to 90% efficiency

    @JsonProperty("dailyStorageLoss")
    @JsonPropertyDescription("Daily storage loss rate (in percentage); energy lost per day due to self-discharge; default: 0%")
    @JsonSerialize(using = PercentageJsonConverter.Serializer.class)
    @JsonDeserialize(using = PercentageJsonConverter.Deserializer.class)
    private Percentage dailyStorageLoss = new Percentage(0.05); // Default to 5% daily loss

    @JsonProperty("panelEfficiency")
    @JsonPropertyDescription("Solar panel inverter efficiency (in percentage); energy conversion efficiency; default: 90%")
    @JsonSerialize(using = PercentageJsonConverter.Serializer.class)
    @JsonDeserialize(using = PercentageJsonConverter.Deserializer.class)
    private Percentage panelEfficiency = new Percentage(0.90); // Default to 90% inverter efficiency

    @JsonProperty("usageProfile")
    @JsonPropertyDescription("Usage profile for electric cars defining when the car is away from home")
    private CarUsageProfile usageProfile;

    @JsonProperty("startStorageLevel")
    @JsonPropertyDescription("Initial storage level for batteries and electric cars (in percentage); default: 0%")
    @JsonSerialize(using = PercentageJsonConverter.Serializer.class)
    @JsonDeserialize(using = PercentageJsonConverter.Deserializer.class)
    private Percentage startStorageLevel = Percentage.ZERO; // Default to 0% charge

    public DeviceType getType()
    {
        return type;
    }

    public void setType(DeviceType type)
    {
        this.type = type;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Power getProduction()
    {
        return production;
    }

    public void setProduction(Power production)
    {
        this.production = production;
    }

    public Energy getCapacity()
    {
        return capacity;
    }

    public void setCapacity(Energy capacity)
    {
        this.capacity = capacity;
    }

    public Power getChargeRate()
    {
        return chargeRate;
    }

    public void setChargeRate(Power chargeRate)
    {
        this.chargeRate = chargeRate;
    }

    public Power getDischargeRate()
    {
        return dischargeRate;
    }

    public void setDischargeRate(Power dischargeRate)
    {
        this.dischargeRate = dischargeRate;
    }

    public Power getConsumption()
    {
        return consumption;
    }

    public void setConsumption(Power consumption)
    {
        this.consumption = consumption;
    }

    public int getCount()
    {
        return count;
    }

    public void setCount(int count)
    {
        this.count = count;
    }

    public String getStatisticsResource()
    {
        return statisticsResource;
    }

    public void setStatisticsResource(String statisticsResource)
    {
        this.statisticsResource = statisticsResource;
    }

    public Percentage getChargingEfficiency()
    {
        return chargingEfficiency;
    }

    public void setChargingEfficiency(Percentage chargingEfficiency)
    {
        this.chargingEfficiency = chargingEfficiency;
    }

    public Percentage getDischargingEfficiency()
    {
        return dischargingEfficiency;
    }

    public void setDischargingEfficiency(Percentage dischargingEfficiency)
    {
        this.dischargingEfficiency = dischargingEfficiency;
    }

    public Percentage getDailyStorageLoss()
    {
        return dailyStorageLoss;
    }

    public void setDailyStorageLoss(Percentage dailyStorageLoss)
    {
        this.dailyStorageLoss = dailyStorageLoss;
    }

    public Percentage getPanelEfficiency()
    {
        return panelEfficiency;
    }

    public void setPanelEfficiency(Percentage panelEfficiency)
    {
        this.panelEfficiency = panelEfficiency;
    }

    public CarUsageProfile getUsageProfile()
    {
        return usageProfile;
    }

    public void setUsageProfile(CarUsageProfile usageProfile)
    {
        this.usageProfile = usageProfile;
    }

    public Percentage getStartStorageLevel()
    {
        return startStorageLevel;
    }

    public void setStartStorageLevel(Percentage startStorageLevel)
    {
        this.startStorageLevel = startStorageLevel;
    }

    @JsonIgnore
    public SolarPanelStatistics getSolarPanelStatistics()
    {
        if (getStatisticsResource() != null)
        {
            if (getStatisticsResource().equals("curved"))
            {
                return CurvePanelStatistic.CURVED;
            }
            if (getStatisticsResource().equals("full"))
            {
                return SolarPanelStatisticsImpl.FULL_EFFICIENCY;
            }
            final SolarPanelStatisticsLoader statisticsLoader = new SolarPanelStatisticsLoader();
            try
            {
                return SolarPanelStatisticsImpl.create(statisticsLoader.load(getStatisticsResource()));
            }
            catch(IOException e)
            {
                throw new RuntimeException(e);
            }
        }
        // Default statistics with full efficiency for all hours when no statistics file is provided
        return SolarPanelStatisticsImpl.FULL_EFFICIENCY;
    }
}