package de.hatoka.eos.hardware.enphase.clients;

import de.hatoka.eos.hardware.enphase.model.DevicesResponseRO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface EnphaseSystemDetailsClient
{
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/devices")
    DevicesResponseRO getDevices(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey);
}
