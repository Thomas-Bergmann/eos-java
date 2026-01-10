package de.hatoka.eos.hardware.enphase.devices;

import de.hatoka.eos.hardware.enphase.auth.EnphaseAuthentication;
import de.hatoka.eos.hardware.enphase.clients.EnphaseSystemDetailsClient;
import de.hatoka.eos.hardware.enphase.config.EnphaseConfiguration;
import de.hatoka.eos.hardware.enphase.model.DevicesResponseRO;
import de.hatoka.eos.hardware.enphase.model.EnchargeRO;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.eclipse.microprofile.rest.client.inject.RestClient;

import java.util.List;

@Singleton
public class EnphaseBatteryReader
{
    @Inject
    private EnphaseAuthentication authentication;
    @Inject
    private EnphaseConfiguration configuration;

    @Inject
    @RestClient
    private EnphaseSystemDetailsClient restClient;

    List<EnchargeRO> getBatteries()
    {
        DevicesResponseRO devices = restClient.getDevices(authentication.getAuthorization(), configuration.getSystemId(), configuration.getApiKey());
        return devices.getDevices().getEncharges();
    }
}
