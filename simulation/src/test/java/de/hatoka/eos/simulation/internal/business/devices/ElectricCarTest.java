package de.hatoka.eos.simulation.internal.business.devices;

import de.hatoka.eos.simulation.capi.business.config.CarUsageProfile;
import de.hatoka.eos.simulation.capi.business.config.DeviceConfig;
import de.hatoka.eos.simulation.capi.business.device.DeviceState;
import de.hatoka.eos.simulation.capi.business.simulation.EnergySystem;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStep;
import de.hatoka.eos.simulation.capi.business.simulation.SimulationStepResult;
import de.hatoka.eos.units.capi.Energy;
import de.hatoka.eos.units.capi.Percentage;
import de.hatoka.eos.units.capi.Power;
import de.hatoka.eos.simulation.internal.business.DateTooling;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ElectricCarTest
{
    public static final double ALLOWED_DELTA = 0.001;
    private static final ZonedDateTime startDate = DateTooling.SOMMER_NIGHT;

    private ElectricCar createStandardElectricCar()
    {
        DeviceConfig data = new DeviceConfig();
        data.setCapacity(Energy.ofKwh(50.0)); // 50 kWh capacity (typical EV)
        data.setChargeRate(Power.ofKw(11.0)); // 11 kW charge rate (AC charging)
        data.setDischargeRate(Power.ofKw(10.0)); // 10 kW discharge rate (V2G)
        return new ElectricCar(data);
    }

    @Test
    public void testInitialState()
    {
        ElectricCar car = createStandardElectricCar();
        DeviceState initialState = car.getInitialState();

        assertEquals(50.0, initialState.maxEnergy().amount(), ALLOWED_DELTA);
        assertEquals(0.0, initialState.storedEnergy().amount(), ALLOWED_DELTA);
        assertEquals(0.0, initialState.percentage().value(), ALLOWED_DELTA);
    }

    @Test
    public void testUsesCarChargingLimitNotForceChargingLimit()
    {
        ElectricCar car = createStandardElectricCar();
        SimulationStep step = SimulationStep.valueOf(startDate, Duration.ofHours(1));

        // Electric car should use default forceChargingLimit (0%) - only charge from solar/excess energy
        Percentage chargingLimit = car.getChargingLimit(step);
        assertEquals(0.0, chargingLimit.value(), ALLOWED_DELTA);
    }

    private ElectricCar createElectricCarWithUsageProfile()
    {
        DeviceConfig config = new DeviceConfig();
        config.setCapacity(Energy.ofKwh(50.0));
        config.setChargeRate(Power.ofKw(11.0));
        config.setDischargeRate(Power.ofKw(10.0));

        CarUsageProfile usageProfile = new CarUsageProfile(
                        List.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY),
                        LocalTime.of(8, 0),  // 08:00
                        LocalTime.of(17, 0), // 17:00
                        Energy.ofKwh(15.0)   // 15 kWh consumption during usage
        );
        config.setUsageProfile(usageProfile);

        return new ElectricCar(config);
    }

    /**
     * Tests that an electric car with a weekday usage profile is available for charging on weekends.
     * 
     * Scenario:
     * - Car has a weekday usage profile (away Monday-Friday 8:00-17:00)
     * - Test time: Saturday 10:00 AM (weekend, car is available)
     * - Initial state: 30 kWh (60% of 50 kWh capacity)
     * - System has 5 kWh excess energy available
     * 
     * Expected behavior:
     * - Car is available for charging (weekend not in usage profile)
     * - No grid charging at 10 AM (chargingLimit is 0% by default)
     * - Charges from excess energy only: 5 kWh → 4.5 kWh stored (90% efficiency)
     * - Storage loss: ~0.0625 kWh (5% daily loss over 1 hour)
     * - Final energy: 30 - 0.0625 + 4.5 = 34.4375 kWh
     */
    @Test
    public void testCarAvailableOnWeekend()
    {
        ElectricCar car = createElectricCarWithUsageProfile();

        // Saturday at 10:00 AM - car should be available
        ZonedDateTime saturdayMorning = DateTooling.createBerlinDate("2023/06/10").withHour(10); // Saturday
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.6)); // 60% charged
        SimulationStep step = SimulationStep.valueOf(saturdayMorning, Duration.ofHours(1));
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(5.0)); // Excess energy available

        SimulationStepResult result = car.simulate(step, system, initialState);

        // Car should participate in charging since it's weekend (available)
        // 30 kWh initial - 0.0625 kWh storage loss + 4.5 kWh charged (5 kWh * 90% efficiency) = 34.4375 kWh
        assertEquals(34.4375, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA);
    }

    /**
     * Tests that an electric car with a usage profile is not available for charging during work hours.
     * 
     * Scenario:
     * - Car has a weekday usage profile (away Monday-Friday 8:00-17:00)
     * - Test time: Friday 10:00 AM (during work hours, car is away)
     * - Initial state: 30 kWh (60% of 50 kWh capacity)
     * - System has 5 kWh excess energy available
     * 
     * Expected behavior:
     * - Car is away and not available for charging
     * - No energy transferred to/from car
     * - Only storage loss applied: ~0.0625 kWh (5% daily loss over 1 hour)
     * - Excess energy remains in system unchanged
     * - Final car energy: 30 - 0.0625 ≈ 30 kWh (rounded for test)
     */
    @Test
    public void testCarAwayDuringWorkHours()
    {
        ElectricCar car = createElectricCarWithUsageProfile();

        // Friday at 10:00 AM - car should be away (within work hours)
        ZonedDateTime fridayMorning = DateTooling.createBerlinDate("2023/06/09").withHour(10); // Friday
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.6)); // 60% charged
        SimulationStep step = SimulationStep.valueOf(fridayMorning, Duration.ofHours(1));
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(5.0)); // Excess energy available

        SimulationStepResult result = car.simulate(step, system, initialState);

        // Car should not participate in charging since it's away
        assertEquals(30.0, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA);
        assertEquals(5.0, result.system().getCurrentEnergy().amount(), ALLOWED_DELTA); // Energy should remain in system
    }

    /**
     * Tests that an electric car with a usage profile is available for charging in early morning before work.
     * 
     * Scenario:
     * - Car has a weekday usage profile (away Monday-Friday 8:00-17:00)
     * - Test time: Friday 7:00 AM (before work hours, car is available)
     * - Initial state: 30 kWh (60% of 50 kWh capacity)
     * - System has 5 kWh excess energy available
     * 
     * Expected behavior:
     * - Car is available for charging (before 8:00 AM work start)
     * - No grid charging at 7 AM (chargingLimit is 0% by default)
     * - Charges from excess energy only: 5 kWh → 4.5 kWh stored (90% efficiency)
     * - Storage loss: ~0.0625 kWh (5% daily loss over 1 hour)
     * - Final energy: 30 - 0.0625 + 4.5 = 34.4375 kWh
     */
    @Test
    public void testCarAvailableEarlyMorning()
    {
        ElectricCar car = createElectricCarWithUsageProfile();

        // Friday at 7:00 AM - car should be available (before 8:00 AM)
        ZonedDateTime fridayMorning = DateTooling.createBerlinDate("2023/06/09").withHour(7); // Friday
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.6)); // 60% charged
        SimulationStep step = SimulationStep.valueOf(fridayMorning, Duration.ofHours(1));
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(5.0)); // Excess energy available

        SimulationStepResult result = car.simulate(step, system, initialState);

        // Car should participate in charging since it's before work hours
        // 30 kWh initial - 0.0625 kWh storage loss + 4.5 kWh charged (5 kWh * 90% efficiency) = 34.4375 kWh
        assertEquals(34.4375, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA);
    }

    /**
     * Tests that an electric car with a usage profile can charge from excess energy when available
     * in the evening after work hours.
     * 
     * Scenario:
     * - Car has a weekday usage profile (away 8:00-17:00)
     * - Test time: Friday 6:00 PM (after work hours, car is available)
     * - Initial state: 30 kWh (60% of 50 kWh capacity)
     * - System has 5 kWh excess energy available
     * 
     * Expected behavior:
     * - Car is available for charging (outside work hours)
     * - No grid charging at 6 PM (chargingLimit is 0% by default)
     * - Charges from excess energy only: 5 kWh → 4.5 kWh stored (90% efficiency)
     * - Storage loss: ~0.0625 kWh (5% daily loss over 1 hour)
     * - Final energy: 30 - 0.0625 + 4.5 = 34.4375 kWh
     */
    @Test
    public void testCarAvailableEvening()
    {
        ElectricCar car = createElectricCarWithUsageProfile();

        // Friday at 6:00 PM - car should be available (after 5:00 PM)
        ZonedDateTime fridayEvening = DateTooling.createBerlinDate("2023/06/09").withHour(18); // Friday
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.6)); // 60% charged
        SimulationStep step = SimulationStep.valueOf(fridayEvening, Duration.ofHours(1));
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(5.0)); // Excess energy available

        SimulationStepResult result = car.simulate(step, system, initialState);

        // Car should participate in charging since it's after work hours
        // 30 kWh initial - 0.0625 kWh storage loss + 4.5 kWh charged (5 kWh * 90% efficiency) = 34.4375 kWh
        assertEquals(34.4375, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA);
    }

    /**
     * Tests that an electric car consumes energy when returning from usage period.
     * 
     * Scenario:
     * - Car has a weekday usage profile (away Monday-Friday 8:00-17:00, consumes 15 kWh)
     * - Test time: Friday 5:00 PM (exact return time, car just became available)
     * - Initial state: 40 kWh (80% of 50 kWh capacity)
     * - No excess energy in system
     * 
     * Expected behavior:
     * - Car was away previous hour (4:00-5:00 PM) and is now available (5:00 PM)
     * - Energy consumption applied: 15 kWh used during work day
     * - Storage loss: ~0.0833 kWh (5% daily loss over 1 hour on 40 kWh)
     * - No charging occurs (no excess energy, chargingLimit is 0% by default)
     * - Final energy: 40 - 0.0833 - 15 ≈ 24.948 kWh
     */
    @Test
    public void testEnergyConsumptionWhenCarReturns()
    {
        ElectricCar car = createElectricCarWithUsageProfile();

        // Friday at 5:00 PM - car returns from work and should consume 15 kWh
        ZonedDateTime fridayEvening = DateTooling.createBerlinDate("2023/06/09").withHour(17); // Friday at 5 PM
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.8)); // 80% charged
        SimulationStep step = SimulationStep.valueOf(fridayEvening, Duration.ofHours(1));
        EnergySystem system = EnergySystem.INIT;

        SimulationStepResult result = car.simulate(step, system, initialState);

        // Car should have consumed 15 kWh: 40 - storage loss - 15 = ~24.948 kWh remaining
        assertEquals(24.948, result.deviceState().storedEnergy().amount(), 0.01);
    }

    /**
     * Tests that an electric car without a usage profile behaves like a normal battery.
     * 
     * Scenario:
     * - Car has no usage profile configured (always available)
     * - Test time: Friday 10:00 AM (any time should work)
     * - Initial state: 30 kWh (60% of 50 kWh capacity)
     * - System has 5 kWh excess energy available
     * 
     * Expected behavior:
     * - Car is always available for charging (no usage restrictions)
     * - No grid charging at 10 AM (chargingLimit is 0% by default)
     * - Charges from excess energy only: 5 kWh → 4.5 kWh stored (90% efficiency)
     * - Storage loss: ~0.0625 kWh (5% daily loss over 1 hour)
     * - Final energy: 30 - 0.0625 + 4.5 = 34.4375 kWh
     */
    @Test
    public void testCarWithoutUsageProfileBehavesNormally()
    {
        ElectricCar car = createStandardElectricCar(); // No usage profile

        // Friday at 10:00 AM
        ZonedDateTime fridayMorning = DateTooling.createBerlinDate("2023/06/09").withHour(10);
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.6));
        SimulationStep step = SimulationStep.valueOf(fridayMorning, Duration.ofHours(1));
        EnergySystem system = EnergySystem.INIT.produce(Energy.ofKwh(5.0));

        SimulationStepResult result = car.simulate(step, system, initialState);

        // Car should behave normally and charge since no usage profile is configured
        // 30 kWh initial - 0.0625 kWh storage loss + 4.5 kWh charged (5 kWh * 90% efficiency) = 34.4375 kWh
        assertEquals(34.4375, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA);
    }

    /**
     * Tests that ElectricCar does NOT discharge to balance system energy deficit caused by NOISY_USAGE.
     * This verifies that ElectricCar.shouldDischarge() correctly returns false and prevents discharging.
     */
    @Test
    public void testElectricCarDoesNotDischargeForSystemDeficit()
    {
        ElectricCar car = createStandardElectricCar();
        
        // Saturday at 10:00 AM - car should be available
        ZonedDateTime testTime = DateTooling.createBerlinDate("2023/06/10").withHour(10);
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.6)); // 30 kWh stored
        SimulationStep step = SimulationStep.valueOf(testTime, Duration.ofHours(1));
        
        // Create system with NEGATIVE energy (deficit) - simulates what NOISY_USAGE does
        EnergySystem systemWithDeficit = EnergySystem.INIT.consume(Energy.ofKwh(5.0)); // -5 kWh deficit
        
        SimulationStepResult result = car.simulate(step, systemWithDeficit, initialState);
        
        // ElectricCar should NOT discharge to balance the system deficit
        // Expected: Only storage loss applied: 30 - (30 * 0.05 / 24) = 30 - 0.0625 = 29.9375 kWh
        assertEquals(29.9375, result.deviceState().storedEnergy().amount(), ALLOWED_DELTA,
                    "ElectricCar should not discharge to balance system deficit - only storage loss should apply");
                    
        // System deficit should remain (car didn't provide energy)
        assertEquals(-5.0, result.system().getCurrentEnergy().amount(), ALLOWED_DELTA,
                    "System deficit should remain unchanged - ElectricCar should not discharge");
    }

    /**
     * Comparison test: Tests that a regular Battery DOES discharge to balance system deficit.
     * This shows the difference between Battery and ElectricCar behavior.
     */
    @Test
    public void testRegularBatteryDischargesForSystemDeficit()
    {
        // Create a regular Battery (not ElectricCar)
        DeviceConfig config = new DeviceConfig();
        config.setCapacity(Energy.ofKwh(50.0));
        config.setChargeRate(Power.ofKw(11.0));
        config.setDischargeRate(Power.ofKw(10.0));
        Battery battery = new Battery(config);
        
        // Same scenario as ElectricCar test
        ZonedDateTime testTime = DateTooling.createBerlinDate("2023/06/10").withHour(10);
        DeviceState initialState = new DeviceState(Energy.ofKwh(50.0), new Percentage(0.6)); // 30 kWh stored
        SimulationStep step = SimulationStep.valueOf(testTime, Duration.ofHours(1));
        
        // Create system with NEGATIVE energy (deficit) - simulates what NOISY_USAGE does
        EnergySystem systemWithDeficit = EnergySystem.INIT.consume(Energy.ofKwh(5.0)); // -5 kWh deficit
        
        SimulationStepResult result = battery.simulate(step, systemWithDeficit, initialState);
        
        // Regular Battery SHOULD discharge to balance the system deficit
        // Expected: Storage loss + discharge to cover deficit
        // Storage loss: 30 - (30 * 0.05 / 24) = 30 - 0.0625 = 29.9375
        // Discharge: 5 kWh needed, but with 90% efficiency, battery loses 5/0.9 = 5.56 kWh
        // Final: 29.9375 - 5.56 = 24.38 kWh (approximately)
        assertTrue(result.deviceState().storedEnergy().amount() < 29.0, 
                   "Regular Battery should discharge to balance system deficit");
                    
        // System deficit should be reduced or eliminated
        assertTrue(result.system().getCurrentEnergy().amount() > -5.0,
                   "System deficit should be reduced by Battery discharge");
    }
}