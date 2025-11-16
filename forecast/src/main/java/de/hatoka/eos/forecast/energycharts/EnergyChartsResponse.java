package de.hatoka.eos.forecast.energycharts;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import java.util.List;

/**
 * Data Transfer Object for Energy Charts API response.
 * Represents the array of data series from the Energy Charts API.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnergyChartsResponse
{
    @JsonProperty("name")
    private Object name;

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("data")
    private List<Double> data;

    public List<Double> getData()
    {
        return data;
    }

    public void setData(List<Double> data)
    {
        this.data = data;
    }

    public String getCurrency()
    {
        return currency;
    }

    public Object getName()
    {
        return name;
    }
}

