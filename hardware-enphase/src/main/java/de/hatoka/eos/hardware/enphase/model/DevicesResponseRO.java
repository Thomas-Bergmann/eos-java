package de.hatoka.eos.hardware.enphase.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DevicesResponseRO
{
    @JsonProperty("system_id")
    private long systemId;
    @JsonProperty("devices")
    private DevicesRO devices;

    public long getSystemId()
    {
        return systemId;
    }

    public DevicesRO getDevices()
    {
        return devices;
    }

    public static class DevicesRO
    {
        @JsonProperty("encharges")
        private List<EnchargeRO> encharges;

        // ... add other device lists as needed ...
        public List<EnchargeRO> getEncharges()
        {
            return encharges;
        }
    }
}

