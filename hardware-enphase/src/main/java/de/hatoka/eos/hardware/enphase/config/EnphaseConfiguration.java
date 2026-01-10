package de.hatoka.eos.hardware.enphase.config;

import jakarta.inject.Singleton;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.File;

@Singleton
public class EnphaseConfiguration
{
    @ConfigProperty(name = "eos.enphase.uri.service", defaultValue = "https://api.enphaseenergy.com")
    private String serviceURI = "https://api.enphaseenergy.com";

    @ConfigProperty(name = "eos.enphase.system")
    private String systemId;

    @ConfigProperty(name = "eos.enphase.api.key")
    private String apiKey;

    @ConfigProperty(name = "eos.enphase.client.id")
    private String clientId;

    @ConfigProperty(name = "eos.enphase.client.secret")
    private String clientSecret;

    @ConfigProperty(name = "eos.enphase.token.file", defaultValue = ".enphase_token")
    private String tokenFile;

    public String getServiceURI()
    {
        return serviceURI;
    }

    public String getSystemId()
    {
        return systemId;
    }

    public String getApiKey()
    {
        return apiKey;
    }

    public String getClientId()
    {
        return clientId;
    }

    public String getClientSecret()
    {
        return clientSecret;
    }

    public File getTokenFile()
    {
        return new File(tokenFile);
    }
}
