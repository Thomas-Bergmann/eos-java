package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.Objects;

@JsonSerialize(using = EnergyJsonConverter.Serializer.class)
@JsonDeserialize(using = EnergyJsonConverter.Deserializer.class)
public record Energy(double amount, EnergyUnits unit)
{
    public static final Energy ZERO = ofKwh(0.0);

    public static Energy ofKwh(double kwh)
    {
        return new Energy(kwh, EnergyUnits.K_WH);
    }

    public static Energy ofWh(double wh)
    {
        return new Energy(wh, EnergyUnits.WH);
    }

    public Energy add(Energy augend)
    {
        return new Energy(augend.convert(unit).amount() + amount, unit);
    }

    public Energy subtract(Energy subtrahend)
    {
        return new Energy(amount - subtrahend.convert(unit).amount(), unit);
    }

    public Energy negate()
    {
        return new Energy(amount == 0 ? 0 : -amount, unit);
    }

    public Energy min(Energy energy)
    {
        return new Energy(Math.min(this.amount, energy.convert(unit).amount), energy.unit);
    }

    public Energy multiply(Percentage percentage)
    {
        return new Energy(amount * percentage.toFraction(), unit);
    }

    public Percentage percentageOf(Energy maxEnergy)
    {
        return new Percentage(amount / maxEnergy.convert(unit).amount);
    }

    public boolean isZero()
    {
        return amount == 0.0;
    }

    public Energy convert(EnergyUnits unit)
    {
        if (unit == this.unit || isZero())
        {
            return this;
        }
        return new Energy(this.amount * this.unit.getBaseUnit() / unit.getBaseUnit(), unit);
    }

    @Override
    public String toString()
    {
        return String.format("%.3f%s", amount, unit.getSymbol());
    }

    public boolean isNegative()
    {
        return amount < 0;
    }

    @Override
    public boolean equals(Object o)
    {
        if (!(o instanceof Energy energy)) return false;
        return Double.compare(amount, energy.convert(unit).amount) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(convert(EnergyUnits.WH).amount);
    }

    /**
     * Converts to a positive amount, doesn't matter if the value is negative or positive
     * @return positive amount of energy
     */
    public Energy absolute()
    {
        if (0 <= amount)
        {
            return this;
        }
        return new Energy(-amount, unit);
    }

    public boolean lessThan(Energy energy)
    {
        return convert(energy.unit).amount < energy.amount;
    }
}
