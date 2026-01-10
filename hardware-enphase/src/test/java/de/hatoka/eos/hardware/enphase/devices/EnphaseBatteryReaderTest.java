package de.hatoka.eos.hardware.enphase.devices;

import de.hatoka.eos.hardware.enphase.model.EnchargeRO;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@QuarkusTest
public class EnphaseBatteryReaderTest
{
    @Inject
    private EnphaseBatteryReader batteryReader;

    @Test
    public void testBatteries()
    {
        List<EnchargeRO> batteries = batteryReader.getBatteries();
        assertTrue(batteries.stream().map(EnchargeRO::getSerialNumber).toList().contains("122433016399"), "known battery doesn't exist");
    }
}

