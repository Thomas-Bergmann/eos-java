# Persistence Memory Module

This module provides in-memory implementations of the Data Access Objects (DAOs) defined in `persistence-api`.

## Purpose

The primary purpose of this module is to enable testing without requiring external dependencies like InfluxDB. It's particularly useful for:

- **CI/CD pipelines** (e.g., GitHub Actions) where you want fast tests without spinning up database services
- **Unit tests** where you want complete control over the data
- **Local development** when you don't want to run InfluxDB

## Implementation

The module provides thread-safe, in-memory implementations using `ConcurrentHashMap`:

- `MemoryWeatherForecastDao` - implements `WeatherForcastDAO`
- `MemoryEnergyStockDao` - implements `EnergyStockDao`

### Key Design Decisions

1. **No Defensive Copying**: The implementations store and return PO objects directly without copying. This is safe because:
   - The value types (`Money`, `Percentage`) are immutable records
   - Even if callers modify the PO objects themselves (via setters), the contained values remain immutable
   - This approach is simpler and more performant than defensive copying

2. **Thread Safety**: Uses `ConcurrentHashMap` for thread-safe access to the storage

3. **Test Utilities**: Each DAO provides a `clear()` method to reset state between tests

## Usage

### In Tests

Add as a test dependency in your module's `build.gradle.kts`:

```kotlin
dependencies {
    // ...
    testImplementation(project(":persistence-memory"))
}
```

### Example

```java
@Test
void testWeatherForecast() {
    MemoryWeatherForecastDao dao = new MemoryWeatherForecastDao();
    
    WeatherForecastKey key = WeatherForecastKey.valueOf(
        WeatherStation.APOLDA,
        ZonedDateTime.now(),
        WeatherDataSource.OPENMETEO
    );
    
    WeatherForecastPO data = new WeatherForecastPO();
    data.setSunProbability(new Percentage(0.75));
    
    dao.update(key, data);
    
    WeatherForecastPO result = dao.get(key);
    assertEquals(0.75, result.getSunProbability().value(), 0.001);
    
    dao.clear(); // Clean up for next test
}
```

## Benefits for CI/CD

With this module, GitHub Actions workflows no longer need to:
- Start InfluxDB service containers
- Wait for database initialization
- Configure database connections

This makes tests faster and more reliable in CI environments.

