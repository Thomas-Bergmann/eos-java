package de.hatoka.eos.units.capi;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class EnergyTest
{
    @Test
    void testConvert()
    {
        assertEquals(1.0005, Energy.ofWh(1000.50).convert(EnergyUnits.K_WH).amount());
        assertEquals(EnergyUnits.K_WH, Energy.ofWh(1000.50).convert(EnergyUnits.K_WH).unit());
    }

}
