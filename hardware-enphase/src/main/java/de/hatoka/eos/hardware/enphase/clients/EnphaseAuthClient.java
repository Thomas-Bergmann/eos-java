package de.hatoka.eos.hardware.enphase.clients;

import de.hatoka.eos.hardware.enphase.auth.EnphaseToken;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface EnphaseAuthClient
{
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/oauth/token")
    EnphaseToken refreshToken(@HeaderParam("Authorization") String authorization, @FormParam("grant_type") String grantType,
                    @FormParam("refresh_token") String refreshToken);
}
