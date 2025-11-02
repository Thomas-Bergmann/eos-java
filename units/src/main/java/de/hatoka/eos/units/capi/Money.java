package de.hatoka.eos.units.capi;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

@JsonSerialize(using = MoneyJsonConverter.Serializer.class)
@JsonDeserialize(using = MoneyJsonConverter.Deserializer.class)
public record Money(BigDecimal amount, String currencyMnemonic)
{
    private static final int MAX_MONEY_DECIMALS = 4;

    public static final Money ZERO = new Money(BigDecimal.ZERO, "EUR");

    /**
     * Creates a Money instance from EUR amount. (only for testing purposes!)
     * @param euros the amount in EUR
     * @return Money instance
     */
    public static Money ofEur(double euros)
    {
        return new Money(BigDecimal.valueOf(euros), "EUR");
    }

    public Money add(Money augend)
    {
        if (!this.currencyMnemonic.equals(augend.currencyMnemonic))
        {
            throw new IllegalArgumentException(
                            "Cannot add Money with different currencies: " + this.currencyMnemonic + " and " + augend.currencyMnemonic);
        }
        return new Money(this.amount.add(augend.amount), this.currencyMnemonic);
    }

    public Money subtract(Money subtrahend)
    {
        if (!this.currencyMnemonic.equals(subtrahend.currencyMnemonic))
        {
            throw new IllegalArgumentException(
                            "Cannot add Money with different currencies: " + this.currencyMnemonic + " and " + subtrahend.currencyMnemonic);
        }
        return new Money(this.amount.subtract(subtrahend.amount), this.currencyMnemonic);
    }

    /**
     * Multiplies this Money amount by a factor.
     * @param factor the multiplication factor
     * @return new Money instance with the product
     */
    public Money multiply(double factor)
    {
        return new Money(this.amount.multiply(BigDecimal.valueOf(factor)), this.currencyMnemonic);
    }

    public Money divide(double divisor)
    {
        return new Money(this.amount.divide(BigDecimal.valueOf(divisor), MAX_MONEY_DECIMALS, RoundingMode.HALF_DOWN), this.currencyMnemonic);
    }

    public Money round()
    {
        return new Money(this.amount.round(new MathContext(this.amount.precision() - this.amount.scale() + 2, RoundingMode.HALF_DOWN)), this.currencyMnemonic);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Money other = (Money) obj;
        return this.currencyMnemonic.equals(other.currencyMnemonic) &&
               this.amount.setScale(MAX_MONEY_DECIMALS, RoundingMode.HALF_DOWN)
                   .equals(other.amount.setScale(MAX_MONEY_DECIMALS, RoundingMode.HALF_DOWN));
    }

    @Override
    public int hashCode()
    {
        return java.util.Objects.hash(
            amount.setScale(MAX_MONEY_DECIMALS, RoundingMode.HALF_DOWN),
            currencyMnemonic
        );
    }

    @Override
    public String toString()
    {
        return String.format("%s %s", round().amount, currencyMnemonic);
    }

    public Money negate()
    {
        return new Money(amount.negate(), currencyMnemonic);
    }
}
