package de.hatoka.eos.devices.capi.business.config;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import de.hatoka.eos.devices.capi.units.Percentage;

/**
 * ChargingConfig defines, how chargeable devices must behave. It's not part of the configuration of the device, because it could be part of the
 * simulation parameter
 */
public class ChargingConfig
{
    /**
     * Configuration for normal charging only (no force charging).
     */
    public static final ChargingConfig ONLY_PRODUCED_ENERGY = ChargingConfig.forceUpTo(Percentage.ZERO);

    /**
     * Configuration for unlimited force charging (up to 100%).
     */
    public static final ChargingConfig FORCE_TO_FULL = ChargingConfig.forceUpTo(Percentage.ONE_HUNDRED);

    /**
     * Configuration for simulation force charging 10% for battery and 90% for car.
     */
    public static final ChargingConfig GOOD = new ChargingConfig(new Percentage(0.1), new Percentage(0.9));

    /**
     * Creates a ChargingConfig with force charging enabled up to the target percentage (for cars and batteries).
     * @param targetPercent target charge level (0.0 to 1.0)
     * @return ChargingConfig with force charging enabled up to the target percentage
     */
    public static ChargingConfig forceUpTo(double targetPercent)
    {
        return forceUpTo(new Percentage(targetPercent));
    }

    /**
     * Creates a ChargingConfig with force charging enabled up to the target percentage (for cars and batteries).
     * @param targetPercent target charge level (0.0 to 1.0)
     * @return ChargingConfig with force charging enabled up to the target percentage
     */
    public static ChargingConfig forceUpTo(Percentage targetPercent)
    {
        return new ChargingConfig(targetPercent, targetPercent);
    }

    @JsonProperty("forceChargingLimit")
    @JsonPropertyDescription("Battery charge level threshold (in percentage); above charging from grid is disabled; default: 0%")
    private Percentage forceChargingLimit = Percentage.ZERO;

    @JsonProperty("carChargingLimit")
    @JsonPropertyDescription("Electric car charge level threshold (in percentage); car charges up to this level; default: 100%")
    private Percentage carChargingLimit = Percentage.ONE_HUNDRED;


    @JsonCreator
    public ChargingConfig()
    {
    }

    private ChargingConfig(Percentage forceChargingLimit, Percentage carChargingLimit)
    {
        this.forceChargingLimit = forceChargingLimit;
        this.carChargingLimit = carChargingLimit;
    }

    public Percentage getForceChargingLimit()
    {
        return forceChargingLimit;
    }

    public void setForceChargingLimit(Percentage forceChargingLimit)
    {
        this.forceChargingLimit = forceChargingLimit;
    }

    public Percentage getCarChargingLimit()
    {
        return carChargingLimit;
    }

    public void setCarChargingLimit(Percentage carChargingLimit)
    {
        this.carChargingLimit = carChargingLimit;
    }
}