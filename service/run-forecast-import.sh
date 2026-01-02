#!/bin/bash
# Script to run the ForecastImportApplication

cd "$(dirname "$0")"

# Check if InfluxDB is running
if ! curl -s http://localhost:8086/health > /dev/null 2>&1; then
    echo "WARNING: InfluxDB does not appear to be running at http://localhost:8086"
    echo "You can start it with: docker-compose up -d influxdb"
    echo "Continuing anyway..."
fi

echo "Starting Weather Forecast Import..."
java -jar build/quarkus-app/quarkus-run.jar

echo ""
echo "Forecast import completed with exit code: $?"

