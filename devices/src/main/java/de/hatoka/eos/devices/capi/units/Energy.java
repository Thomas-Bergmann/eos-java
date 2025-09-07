package de.hatoka.eos.devices.capi.units;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = EnergyJsonConverter.Serializer.class)
@JsonDeserialize(using = EnergyJsonConverter.Deserializer.class)
public record Energy(double amount, EnergyUnits unit)
{
    public static final Energy ZERO = ofKwh(0.0);
    public static final Energy MAX_VALUE = ofKwh(Double.MAX_VALUE);

    public static Energy ofKwh(double kwh)
    {
        return new Energy(kwh, EnergyUnits.K_WH);
    }

    public Energy add(Energy augend)
    {
        return new Energy(augend.amount() + amount, unit);
    }

    public Energy subtract(Energy subtrahend)
    {
        return new Energy(amount - subtrahend.amount(), unit);
    }

    public Energy negate()
    {
        return new Energy(amount == 0 ? 0 : -amount, unit);
    }

    public Energy min(Energy energy)
    {
        return new Energy(Math.min(this.amount, energy.amount), energy.unit);
    }

    public Energy multiply(Percentage percentage)
    {
        return new Energy(amount * percentage.toFraction(), unit);
    }

    public Percentage percentageOf(Energy maxEnergy)
    {
        return new Percentage(amount / maxEnergy.amount);
    }

    public boolean isZero()
    {
        return amount == 0.0;
    }

    @Override
    public String toString()
    {
        return String.format("%.2f%s", amount, unit.getSymbol());
    }
}
