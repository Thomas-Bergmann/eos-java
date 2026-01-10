package de.hatoka.eos.hardware.enphase.auth;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.HeaderParam;

@RegisterRestClient
@Path("/oauth/token")
public interface EnphaseTokenClient {
    @POST
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    @Produces(MediaType.APPLICATION_JSON)
    EnphaseToken refreshToken(
        @HeaderParam("Authorization") String authorization,
        @FormParam("grant_type") String grantType,
        @FormParam("refresh_token") String refreshToken
    );
}
