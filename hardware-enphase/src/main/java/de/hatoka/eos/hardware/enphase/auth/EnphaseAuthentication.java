package de.hatoka.eos.hardware.enphase.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.hatoka.eos.hardware.enphase.config.EnphaseConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.io.IOException;

@Singleton
public class EnphaseAuthentication
{
    private ObjectMapper mapper = new ObjectMapper();

    @Inject
    private EnphaseConfiguration configuration;

    @Inject
    @RestClient
    EnphaseTokenClient tokenClient;

    public EnphaseToken getToken() throws IOException
    {
        EnphaseToken oldToken = readFromTokenFile();
        EnphaseToken newToken = refreshToken(oldToken);
        writeTokenFile(newToken);
        return newToken;
    }

    private EnphaseToken readFromTokenFile() throws IOException
    {
        return mapper.readValue(configuration.getTokenFile(), EnphaseToken.class);
    }

    private void writeTokenFile(EnphaseToken token) throws IOException
    {
        mapper.writeValue(configuration.getTokenFile(), token);
    }

    private EnphaseToken refreshToken(EnphaseToken oldToken)
    {
        String clientId = configuration.getClientId();
        String clientSecret = configuration.getClientSecret();
        String refreshToken = oldToken != null ? oldToken.getRefresh() : null;
        if (refreshToken == null) {
            throw new IllegalArgumentException("No refresh token available");
        }
        String basicAuth = "Basic " + java.util.Base64.getEncoder().encodeToString(
            (clientId + ":" + clientSecret).getBytes(java.nio.charset.StandardCharsets.UTF_8)
        );
        try {
            return tokenClient.refreshToken(
                basicAuth,
                "refresh_token",
                refreshToken
            );
        } catch (Exception e) {
            throw new RuntimeException("Error refreshing token", e);
        }
    }

}
