# EnergyCharts Importer Implementation

## Overview
The EnergyChartsImporter has been implemented to parse JSON data from the Energy Charts API and extract the "Day Ahead Auction (DE-LU)" electricity price data.

## Implementation Details

### Files Created

1. **EnergyChartsImporter.java** (Updated)
   - Parses JSON response from Energy Charts API
   - Extracts "Day Ahead Auction (DE-LU)" data section
   - Maps 15-minute interval prices to timestamps
   - Calculates the correct week of year and start of week

2. **EnergyChartsResponse.java** (New)
   - DTO for Energy Charts API response items
   - Contains fields: name, currency, unit, color, type, visible, showInLegend, allowCsvDownloadForItem, yAxis, data
   - Uses Jackson annotations for JSON deserialization

3. **NameTranslation.java** (New)
   - DTO for multi-language name translations
   - Contains fields: en, de, fr, it, es (English, German, French, Italian, Spanish)

4. **EnergyChartsImporterTest.java** (New)
   - Tests JSON parsing functionality
   - Verifies "Day Ahead Auction (DE-LU)" section extraction
   - Validates data integrity and translations

## Key Features

- **JSON Parsing**: Uses Jackson ObjectMapper to parse the complex JSON structure
- **Data Extraction**: Finds the specific "Day Ahead Auction (DE-LU)" section by matching the English name
- **Time Mapping**: Maps each price data point to a 15-minute interval timestamp starting from Monday 00:00
- **Week Calculation**: Properly calculates the ISO week number and start of week

## JSON Structure

The API returns an array of data series, each with:
```json
{
  "name": [{"en": "...", "de": "...", ...}],
  "currency": "EUR",
  "unit": "EUR/MWh",
  "data": [price1, price2, ...]
}
```

The implementation searches for the entry where `name[0].en == "Day Ahead Auction (DE-LU)"` and extracts its data array.

## Usage

```java
EnergyChartsImporter importer = new EnergyChartsImporter();
ZonedDateTime date = ZonedDateTime.of(2025, 11, 16, 0, 0, 0, 0, ZoneId.of("Europe/Berlin"));
Map<ZonedDateTime, Double> prices = importer.downloadAndProcessWeatherData(date);
```

This returns a map of timestamps (15-minute intervals) to prices in EUR/MWh.

## Test Data

The test uses `energycharts_2025_11_16.json` which contains real data from Energy Charts for week 46 of 2025.
The "Day Ahead Auction (DE-LU)" section starts at line 4882 and contains price data in EUR/MWh.

