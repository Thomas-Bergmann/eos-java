package de.hatoka.eos.units.capi;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class MoneyTest
{
    @Test
    void testOfMnemonic()
    {
        Money money = Money.ofEur(10.50);
        assertEquals("EUR", money.currencyMnemonic());
    }

    @Test
    void testZero()
    {
        Money zero = Money.ZERO;
        assertEquals(BigDecimal.ZERO, zero.amount());
    }

    @Test
    void testAdd_sameCurrency()
    {
        Money money1 = Money.ofEur(10.50);
        Money money2 = Money.ofEur(5.25);

        Money result = money1.add(money2);

        assertEquals(new BigDecimal("15.75"), result.amount());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testAdd_differentCurrencies_throwsException()
    {
        Money money1 = new Money(BigDecimal.TEN, "EUR");
        Money money2 = new Money(BigDecimal.TEN, "USD");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> money1.add(money2)
        );

        assertTrue(exception.getMessage().contains("Cannot add Money with different currencies"));
    }

    @Test
    void testSubtract_sameCurrency()
    {
        Money money1 = Money.ofEur(10.50);
        Money money2 = Money.ofEur(5.25);

        Money result = money1.subtract(money2);

        assertEquals(new BigDecimal("5.25"), result.amount());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testSubtract_differentCurrencies_throwsException()
    {
        Money money1 = new Money(BigDecimal.TEN, "EUR");
        Money money2 = new Money(BigDecimal.TEN, "USD");

        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> money1.subtract(money2)
        );

        assertTrue(exception.getMessage().contains("Cannot add Money with different currencies"));
    }

    @Test
    void testMultiply()
    {
        Money money = Money.ofEur(10.0);

        Money result = money.multiply(2.5);

        assertEquals(new BigDecimal("25.00"), result.amount());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testMultiply_byZero()
    {
        Money money = Money.ofEur(10.0);

        Money result = money.multiply(0);

        assertEquals(BigDecimal.ZERO, result.amount().stripTrailingZeros());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testDivide()
    {
        Money money = Money.ofEur(10.0);

        Money result = money.divide(2.0);

        assertEquals(new BigDecimal("5.0000"), result.amount());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testDivide_withRounding()
    {
        Money money = Money.ofEur(10.0);

        Money result = money.divide(3.0);

        // Should round to 4 decimal places with HALF_DOWN
        assertEquals(new BigDecimal("3.3333"), result.amount());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testNegate()
    {
        Money money = Money.ofEur(10.50);

        Money result = money.negate();

        assertEquals(new BigDecimal("-10.5"), result.amount());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testNegate_negative()
    {
        Money money = Money.ofEur(-10.50);

        Money result = money.negate();

        assertEquals(new BigDecimal("10.5"), result.amount());
        assertEquals("EUR", result.currencyMnemonic());
    }

    @Test
    void testIsLessThan_true()
    {
        Money smaller = Money.ofEur(5.0);
        Money larger = Money.ofEur(10.0);

        assertTrue(smaller.isLessThan(larger));
    }

    @Test
    void testIsLessThan_false()
    {
        Money smaller = Money.ofEur(5.0);
        Money larger = Money.ofEur(10.0);

        assertFalse(larger.isLessThan(smaller));
    }

    @Test
    void testIsLessThan_equal()
    {
        Money money1 = Money.ofEur(10.0);
        Money money2 = Money.ofEur(10.0);

        assertFalse(money1.isLessThan(money2));
    }

    @Test
    void testRound()
    {
        Money money = new Money(new BigDecimal("10.12345"), "EUR");

        Money result = money.round();

        // Should round to 2 decimal places based on precision calculation
        assertEquals(new BigDecimal("10.12"), result.amount());
    }

    @Test
    void testEquals_equalValues()
    {
        Money money1 = Money.ofEur(10.50);
        Money money2 = Money.ofEur(10.50);

        assertEquals(money1, money2);
    }

    @Test
    void testEquals_differentAmounts()
    {
        Money money1 = Money.ofEur(10.50);
        Money money2 = Money.ofEur(10.51);

        assertNotEquals(money1, money2);
    }

    @Test
    void testEquals_differentCurrencies()
    {
        Money money1 = new Money(BigDecimal.TEN, "EUR");
        Money money2 = new Money(BigDecimal.TEN, "USD");

        assertNotEquals(money1, money2);
    }

    @Test
    void testEquals_null()
    {
        Money money = Money.ofEur(10.0);

        assertNotEquals(null, money);
    }

    @Test
    void testNotEquals_withoutRounding()
    {
        Money money1 = new Money(new BigDecimal("10.12345"), "EUR");
        Money money2 = new Money(new BigDecimal("10.12346"), "EUR");

        assertNotEquals(money1, money2);
    }

    @Test
    void testEquals_withRounding()
    {
        Money money1 = new Money(new BigDecimal("10.12345"), "EUR");
        Money money2 = new Money(new BigDecimal("10.12346"), "EUR");

        assertEquals(money1.round(), money2.round());
    }

    @Test
    void testHashCode_equalObjects()
    {
        Money money1 = Money.ofEur(10.50);
        Money money2 = Money.ofEur(10.50);

        assertEquals(money1.hashCode(), money2.hashCode());
    }

    @Test
    void testZeroOperations()
    {
        Money zero = Money.ZERO;
        Money money = Money.ofEur(10.0);

        assertEquals(money, zero.add(money));
        assertEquals(money, money.add(zero));
        assertEquals(money.negate(), zero.subtract(money));
    }

    @Test
    void testIsLessThan()
    {
        Money money1 = Money.ofEur(10.50);
        Money money2 = Money.ofEur(10.52);

        assertTrue(money1.isLessThan(money2));
    }
}

