# Forecast Import Application

This Quarkus command-line application imports weather forecast data from multiple sources into InfluxDB.

## Features

- **MeteoMedia Importer**: Downloads weather forecast PNG images and extracts sunshine duration data (covers today and tomorrow)
- **OpenMeteo Importer**: Fetches weather forecast data from the OpenMeteo REST API (covers up to 3 days)

## Prerequisites

1. **InfluxDB** must be running and accessible at `http://localhost:8086`
   - You can start it using Docker: `docker-compose up -d influxdb`
   - Or configure a different URL in `src/main/resources/application.properties`

2. **Java 17+** must be installed

## Building

```bash
# Build the application
./gradlew :forecast:quarkusBuild

# Or build everything
./gradlew build
```

## Running

### Option 1: Using the convenience script
```bash
cd forecast
./run-forecast-import.sh
```

### Option 2: Direct Java execution
```bash
cd forecast
java -jar build/quarkus-app/quarkus-run.jar
```

### Option 3: Using Gradle
```bash
./gradlew :forecast:quarkusRun
```

## Configuration

Configuration is in `forecast/src/main/resources/application.properties`:

```properties
# InfluxDB Configuration
eos.influxdb.url=http://localhost:8086
eos.influxdb.token=eos-token-12345678901234567890
eos.influxdb.org=eos
eos.influxdb.bucket=forecast

# Quarkus Configuration
quarkus.log.level=INFO
quarkus.log.category."de.hatoka.eos".level=DEBUG
```

## Weather Stations

The application imports data for these weather stations:
- **APOLDA**: MeteoMedia station
- **LEIPZIG_STADTWERKE**: MeteoMedia station

Both stations are imported from both MeteoMedia and OpenMeteo sources.

## Output

The application:
1. Downloads weather forecast data from MeteoMedia (PNG images)
2. Downloads weather forecast data from OpenMeteo (JSON via REST API)
3. Converts the data to sunshine duration (minutes per hour)
4. Stores the data in InfluxDB under the configured bucket
5. Exits with code 0 on success, 1 on failure

## Logs

Logs are written to stdout with DEBUG level for the `de.hatoka.eos` package.

## Development

To run in development mode with live reload:
```bash
./gradlew :forecast:quarkusDev
```

Press `q` to quit dev mode.

