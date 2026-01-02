package de.hatoka.eos.forecast.energycharts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EnergyChartsResponseTest
{
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void testParseEnergyChartsResponse() throws Exception
    {
        // Load the test JSON file
        InputStream inputStream = getClass().getResourceAsStream("/energycharts_2025_11_16.json");
        assertNotNull(inputStream, "Test resource file not found");

        String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);

        // Parse JSON array
        List<EnergyChartsResponse> responses = objectMapper.readValue(
            jsonContent,
            new TypeReference<>() {}
        );

        assertNotNull(responses);
        assertFalse(responses.isEmpty());

        EnergyChartsResponse dayAheadAuction = EnergyChartsImporter.findDayAheadAuction(responses);
        assertNotNull(dayAheadAuction, "Day Ahead Auction (DE-LU) section not found");

        // Verify the data
        assertEquals("EUR", dayAheadAuction.getCurrency());
        assertNotNull(dayAheadAuction.getData());
        assertFalse(dayAheadAuction.getData().isEmpty());

        // Check first few data points match expected values
        List<Double> data = dayAheadAuction.getData();
        assertFalse(data.isEmpty());
        assertEquals(92.38, data.get(0), 0.001);
        assertEquals(89.98, data.get(1), 0.001);
        assertEquals(87.4, data.get(2), 0.001);
    }
}

