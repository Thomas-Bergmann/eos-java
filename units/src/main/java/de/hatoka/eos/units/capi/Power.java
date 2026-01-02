package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.time.Duration;

/**
 * Represents a power value with a specific unit. The unit is typically in kilowatts (kW).
 */
@JsonSerialize(using = PowerJsonConverter.Serializer.class)
@JsonDeserialize(using = PowerJsonConverter.Deserializer.class)
public record Power(double amount, PowerUnits unit)
{
    public static final Power MAX_VALUE = new Power(Integer.MAX_VALUE, PowerUnits.K_W);

    public static Power ofKw(double amount)
    {
        return new Power(amount, PowerUnits.K_W);
    }

    public Energy multiply(Duration duration)
    {
        return new Energy(amount() * ((double)duration.toMinutes() / 60), EnergyUnits.K_WH);
    }

    public Power multiply(Percentage percentage)
    {
        return new Power(amount() * percentage.toFraction(), unit);
    }
}
