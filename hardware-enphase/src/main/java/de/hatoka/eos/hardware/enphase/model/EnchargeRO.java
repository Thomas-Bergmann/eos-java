package de.hatoka.eos.hardware.enphase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Battery devices
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnchargeRO
{
    @JsonProperty("serial_number")
    private String serialNumber;

    public String getSerialNumber()
    {
        return serialNumber;
    }
}
