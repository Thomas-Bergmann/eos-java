package de.hatoka.eos.devices.capi.units;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * Percentage represents a value between 0.0 and 1.0, where 0.0 is 0% and 1.0 is 100%.
 * It can be used to represent fractions or percentages in a consistent manner.
 * @param value the percentage value as a fraction (0.0 to 1.0)
 */
@JsonSerialize(using = PercentageJsonConverter.Serializer.class)
@JsonDeserialize(using = PercentageJsonConverter.Deserializer.class)
public record Percentage(double value)
{
    public static final Percentage ZERO = new Percentage(0.0);
    public static final Percentage ONE_HUNDRED = new Percentage(1.0);

    public Percentage
    {
        if (value < 0.0 || value > 1.0)
        {
            throw new IllegalArgumentException("Percentage must be between 0 and 1: is " + value);
        }
    }

    /**
     * @return the percentage value as a fraction (0.0 to 1.0)
     */
    public double toFraction()
    {
        return value;
    }

    /**
     * @return the percentage value as a percentage (0.0 to 100.0)
     */
    public double toPercentage()
    {
        return value * 100.0;
    }

    public Percentage subtract(Percentage subtrahend)
    {
        return new Percentage(value - subtrahend.value);
    }

    @Override
    public String toString()
    {
        return String.format("%.2f%%", toPercentage());
    }

    public Percentage multiply(double factor)
    {
        return new Percentage(value * factor);
    }

    /**
     * @param percentage percentage to compare
     * @return true if the current percentage smaller than the given
     */
    public boolean lessThan(Percentage percentage)
    {
        return this.value < percentage.value;
    }
}
