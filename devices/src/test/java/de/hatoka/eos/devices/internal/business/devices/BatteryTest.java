package de.hatoka.eos.devices.internal.business.devices;

import de.hatoka.eos.devices.capi.business.config.DeviceConfig;
import de.hatoka.eos.devices.capi.business.device.DeviceState;
import de.hatoka.eos.devices.capi.business.simulation.EnergySystem;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStep;
import de.hatoka.eos.devices.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.units.capi.Power;
import de.hatoka.eos.devices.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BatteryTest
{
    public static final double ALLOWED_DELTA = 0.001;
    private static final ZonedDateTime startDate = DateTooling.SOMMER_NIGHT;

    private DeviceConfig getStandardBatteryConfig()
    {
        DeviceConfig config = new DeviceConfig();
        config.setCapacity(Energy.ofKwh(10.0)); // 10 kWh capacity
        config.setChargeRate(Power.ofKw(5.0)); // 5 kW charge rate
        config.setDischargeRate(Power.ofKw(4.0)); // 4 kW discharge rate
        // Set 100% efficiency and 0% storage loss for tests
        config.setChargingEfficiency(Percentage.ONE_HUNDRED);
        config.setDischargingEfficiency(Percentage.ONE_HUNDRED);
        config.setDailyStorageLoss(Percentage.ZERO);
        return config;
    }

    private Battery createStandardBattery()
    {
        return new Battery(getStandardBatteryConfig());
    }

    private Battery createStandardBattery(Percentage forceCharging)
    {
        DeviceConfig config = getStandardBatteryConfig();
        config.setForceChargingLimit(forceCharging);
        return new Battery(config);
    }

    private static DeviceState createStandardDeviceState(Percentage percentage)
    {
        return new DeviceState(Energy.ofKwh(10.0), percentage); // 10 kWh capacity
    }

    private SimulationStep createSimulationStep(Duration duration)
    {
        return SimulationStep.valueOf(startDate, duration);
    }

    @Test
    public void testSimulateChargingWithExcessSystemEnergy()
    {
        // Arrange
        Battery battery = createStandardBattery();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1)); 
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.5)); // 50% charged
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(2.0)); // 2 kWh excess

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should charge with 2.0 kWh (limited by available excess energy)
        assertNotNull(result);
        assertEquals(2.0, result.system().charged().amount(), ALLOWED_DELTA); // Battery charged 2.0 kWh
        assertEquals(7.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 5.0 + 2.0 = 7.0 kWh
        assertEquals(0.7, result.deviceState().percentage().value(), ALLOWED_DELTA); // 7.0/10.0 = 0.7
    }

    @Test
    public void testSimulateDischargingWhenEnergyNeeded()
    {
        // Arrange
        Battery battery = createStandardBattery();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1)); 
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.6)); // 60% charged
        EnergySystem system = EnergySystem.INIT.consume(Energy.ofKwh(2.5)); // System needs 2.5 kWh

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should discharge 2.5 kWh to meet system needs
        assertNotNull(result);
        assertEquals(2.5, result.system().discharged().amount(), ALLOWED_DELTA); // Battery discharged 2.5 kWh
        assertEquals(3.5, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 6.0 - 2.5 = 3.5 kWh
        assertEquals(0.35, result.deviceState().percentage().value(), ALLOWED_DELTA); // 3.5/10.0 = 0.35
    }

    @Test
    public void testSimulateFullyChargedBatteryCannotChargeMore()
    {
        // Arrange
        Battery battery = createStandardBattery();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1)); 
        DeviceState deviceState = createStandardDeviceState(Percentage.ONE_HUNDRED); // 100% charged
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(3.0)); // 3 kWh excess available

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery cannot charge beyond capacity
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().charged()); // No charging possible
        assertEquals(10.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // Remains at full capacity
        assertEquals(1.0, result.deviceState().percentage().value(), ALLOWED_DELTA); // Remains at 100%
    }

    @Test
    public void testSimulateEmptyBatteryCannotDischarge()
    {
        // Arrange
        Battery battery = createStandardBattery();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1)); 
        DeviceState deviceState = createStandardDeviceState(Percentage.ZERO); // 0% charged
        EnergySystem system = EnergySystem.INIT.consume(Energy.ofKwh(3.0)); // System needs 3 kWh

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery cannot discharge when empty
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().discharged()); // No energy to provide
        assertEquals(Energy.ZERO, result.deviceState().storedEnergy()); // Remains empty
        assertEquals(0.0, result.deviceState().percentage().value(), ALLOWED_DELTA); // Remains at 0%
    }

    @Test
    public void testSimulateChargingLimitedByPowerRating()
    {
        // Arrange - Different battery with low power charging
        DeviceConfig config = getStandardBatteryConfig();
        config.setChargeRate(Power.ofKw(2.0)); // Only 2 kW charge rate
        Battery battery = new Battery(config);

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1)); 
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.3)); // 30% charged
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(5.0)); // 5 kWh excess available

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery charging limited to 2 kW * 1 hour = 2 kWh (not all 5 kWh available)
        assertNotNull(result);
        assertEquals(2.0, result.system().charged().amount(), ALLOWED_DELTA); // Limited by charging power
        assertEquals(5.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 3.0 + 2.0 = 5.0 kWh
        assertEquals(0.5, result.deviceState().percentage().value(), ALLOWED_DELTA); // 5.0/10.0 = 0.5
    }

    @Test
    public void testSimulatePartialChargingDueToCapacityLimit()
    {
        // Arrange - Battery near full capacity
        Battery battery = createStandardBattery();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.85)); // 85% charged (1.5 kWh space left)
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(4.0)); // 4 kWh excess available

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery charging limited to remaining capacity (1.5 kWh)
        assertNotNull(result);
        assertEquals(1.5, result.system().charged().amount(), ALLOWED_DELTA); // Limited by remaining capacity
        assertEquals(10.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 8.5 + 1.5 = 10.0 kWh (full)
        assertEquals(1.0, result.deviceState().percentage().value(), ALLOWED_DELTA); // 10.0/10.0 = 1.0 (100%)
    }

    @Test
    public void testSimulatePartialDischargingDueToStorageLimit()
    {
        // Arrange - Battery with limited stored energy
        Battery battery = createStandardBattery();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1)); 
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.2)); // Only 2 kWh stored
        EnergySystem system = EnergySystem.INIT.consume(Energy.ofKwh(5.0)); // System needs 5 kWh

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery discharge limited to available storage (2 kWh)
        assertNotNull(result);
        assertEquals(2.0, result.system().discharged().amount(), ALLOWED_DELTA); // Can only provide what's stored
        assertEquals(Energy.ZERO, result.deviceState().storedEnergy()); // 2.0 - 2.0 = 0.0 kWh (empty)
        assertEquals(0.0, result.deviceState().percentage().value(), ALLOWED_DELTA); // 0.0/10.0 = 0.0 (0%)
    }

    @Test
    public void testSimulateBalancedSystemNoAction()
    {
        // Arrange - System is balanced (no excess or deficit)
        Battery battery = createStandardBattery();

        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1)); 
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.5)); // 50% charged
        EnergySystem system = EnergySystem.INIT; // Balanced system

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - No action needed when system is balanced
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().charged());
        assertEquals(Energy.ZERO, result.system().discharged());
        assertEquals(5.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // State unchanged
        assertEquals(0.5, result.deviceState().percentage().value(), ALLOWED_DELTA); // State unchanged
    }

    @Test
    public void testSimulateForcedChargingIgnoresSystemEnergy()
    {
        // Arrange - System has no energy but forced charging is enabled
        Battery battery = createStandardBattery(Percentage.ONE_HUNDRED);

        SimulationStep step = createSimulationStep(Duration.ofHours(1)); // Forced charging enabled
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.3)); // 30% charged
        EnergySystem system = EnergySystem.INIT; // No system energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should charge at maximum rate regardless of system energy
        assertNotNull(result);
        assertEquals(5.0, result.system().charged().amount(), ALLOWED_DELTA); // Charges at max rate: 5 kW * 1 hour = 5 kWh
        assertEquals(8.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 3.0 + 5.0 = 8.0 kWh
        assertEquals(0.8, result.deviceState().percentage().value(), ALLOWED_DELTA); // 8.0/10.0 = 0.8
    }

    @Test
    public void testSimulateForcedChargingLimitedByCapacity()
    {
        // Arrange - Forced charging near full capacity
        Battery battery = createStandardBattery(Percentage.ONE_HUNDRED);

        SimulationStep step = createSimulationStep(Duration.ofHours(1)); // Forced charging enabled
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.9)); // 90% charged (1 kWh space left)
        EnergySystem system = EnergySystem.INIT; // No system energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery charging limited by remaining capacity (1 kWh), not power
        assertNotNull(result);
        assertEquals(1.0, result.system().charged().amount(), ALLOWED_DELTA); // Limited by capacity to 1 kWh
        assertEquals(10.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 9.0 + 1.0 = 10.0 kWh (full)
        assertEquals(1.0, result.deviceState().percentage().value(), ALLOWED_DELTA); // 10.0/10.0 = 1.0 (100%)
    }

    @Test
    public void testSimulateForcedChargingWithNegativeSystemEnergy()
    {
        // Arrange - System needs energy but forced charging is enabled (should still charge)
        Battery battery = createStandardBattery(Percentage.ONE_HUNDRED);

        SimulationStep step = createSimulationStep(Duration.ofHours(1)); // System deficit + forced charging
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.2)); // 20% charged
        EnergySystem system = EnergySystem.INIT.consume(Energy.ofKwh(3.0)); // System needs energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should still charge at max rate despite system needing energy
        assertNotNull(result);
        assertEquals(5.0, result.system().charged().amount(), ALLOWED_DELTA); // Charges at max rate: 5 kW * 1 hour = 5 kWh
        assertEquals(7.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 2.0 + 5.0 = 7.0 kWh
        assertEquals(0.7, result.deviceState().percentage().value(), ALLOWED_DELTA); // 7.0/10.0 = 0.7
    }

    @Test
    public void testSimulateForcedChargingWithExcessSystemEnergy()
    {
        // Arrange - System has excess energy and forced charging is enabled
        Battery battery = createStandardBattery(Percentage.ONE_HUNDRED);

        SimulationStep step = createSimulationStep(Duration.ofHours(1)); // System excess + forced charging
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.4)); // 40% charged
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(3.0)); // System has excess energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should charge at max rate, consuming from system (Grid handles the optimization)
        assertNotNull(result);
        assertEquals(5.0, result.system().charged().amount(), ALLOWED_DELTA); // Charges at max rate: 5 kW * 1 hour = 5 kWh
        assertEquals(9.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 4.0 + 5.0 = 9.0 kWh
        assertEquals(0.9, result.deviceState().percentage().value(), ALLOWED_DELTA); // 9.0/10.0 = 0.9
    }

    @Test
    public void testSimulateForceChargingUpToPercentageLimit()
    {
        // Arrange - Force charging up to 60%
        Battery battery = createStandardBattery(new Percentage(0.6));

        SimulationStep step = createSimulationStep(Duration.ofHours(1));
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.3)); // 30% charged
        EnergySystem system = EnergySystem.INIT; // No system energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should charge up to 60% limit
        assertNotNull(result);
        assertEquals(3.0, result.system().charged().amount(), ALLOWED_DELTA); // Limited to reach 60%: 6.0 - 3.0 = 3.0 kWh
        assertEquals(6.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 3.0 + 3.0 = 6.0 kWh (60%)
        assertEquals(0.6, result.deviceState().percentage().value(), ALLOWED_DELTA); // 6.0/10.0 = 0.6
    }

    @Test
    public void testSimulateForceChargingStopsAtPercentageLimit()
    {
        // Arrange - Force charging up to 60%, but battery already at 70%
        Battery battery = createStandardBattery();

        SimulationStep step = createSimulationStep(Duration.ofHours(1));
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.7)); // 70% charged (above limit)
        EnergySystem system = EnergySystem.INIT; // No system energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should not charge since above 60% limit
        assertNotNull(result);
        assertEquals(Energy.ZERO, result.system().charged()); // No charging
        assertEquals(7.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // State unchanged
        assertEquals(0.7, result.deviceState().percentage().value(), ALLOWED_DELTA); // State unchanged
    }

    @Test
    public void testSimulateForceChargingLimitedByBothPercentageAndCapacity()
    {
        // Arrange - Force charging up to 70%, battery at 65%
        Battery battery = createStandardBattery(new Percentage(0.7));

        SimulationStep step = createSimulationStep(Duration.ofHours(1));
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.65)); // 65% charged
        EnergySystem system = EnergySystem.INIT; // No system energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should charge only to 70% (0.5 kWh more)
        assertNotNull(result);
        assertEquals(0.5, result.system().charged().amount(), ALLOWED_DELTA); // Limited to reach 70%: 7.0 - 6.5 = 0.5 kWh
        assertEquals(7.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 6.5 + 0.5 = 7.0 kWh (70%)
        assertEquals(0.7, result.deviceState().percentage().value(), ALLOWED_DELTA); // 7.0/10.0 = 0.7 (70%)
    }

    @Test
    public void testSimulateNormalChargingWithExcessSystemEnergy()
    {
        // Arrange - Normal charging config with system energy available
        Battery battery = createStandardBattery();

        SimulationStep step = createSimulationStep(Duration.ofHours(1));
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.4)); // 40% charged
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(3.0)); // System has excess energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert - Battery should charge normally using available system energy
        assertNotNull(result);
        assertEquals(3.0, result.system().charged().amount(), ALLOWED_DELTA); // Uses available system energy (3.0 kWh)
        assertEquals(7.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 4.0 + 3.0 = 7.0 kWh
        assertEquals(0.7, result.deviceState().percentage().value(), ALLOWED_DELTA); // 7.0/10.0 = 0.7
    }

    @Test
    public void testUsesForceChargingLimitNotCarChargingLimit()
    {
        Battery battery = createStandardBattery(new Percentage(0.1));
        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));
        
        // Battery should use forceChargingLimit (10%), not carChargingLimit (90%)
        Percentage chargingLimit = battery.getChargingLimit(step);
        assertEquals(0.1, chargingLimit.value(), ALLOWED_DELTA);
    }

    @Test
    public void testDeviceSpecificChargingLimit()
    {
        // Arrange - Battery with device-specific charging limit
        DeviceConfig config = getStandardBatteryConfig();
        config.setForceChargingLimit(new Percentage(0.6)); // 60% charging limit in device config
        Battery battery = new Battery(config);
        
        SimulationStep step = createSimulationStep(Duration.ofHours(1)); // Step config has no force charging
        DeviceState deviceState = createStandardDeviceState(new Percentage(0.3)); // 30% charged
        EnergySystem system = EnergySystem.INIT; // No system energy

        // Act
        SimulationStepResult result = battery.simulate(step, system, deviceState);

        // Assert
        assertNotNull(result);

        // Expected: Battery charges from grid up to device-specific limit (60%), charging 3 kWh (30% to 60%)
        assertEquals(-3.0, result.system().getCurrentEnergy().amount(), ALLOWED_DELTA); // System consumed 3 kWh from grid
        assertEquals(0.6, result.deviceState().percentage().value(), ALLOWED_DELTA); // Charged to 60%
        assertEquals(6.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA); // 3 kWh + 3 kWh = 6 kWh
    }
}
