package de.hatoka.eos.hardware.enphase.clients;

import de.hatoka.eos.hardware.enphase.model.ConsumptionResponseRO;
import de.hatoka.eos.hardware.enphase.model.ProductionResponseRO;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface EnphaseSiteLevelProductionMonitoringClient
{
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/consumption_lifetime")
    ConsumptionResponseRO getConsumption(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey, @QueryParam("start_date") String start, @QueryParam("end_date") String end);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/energy_lifetime")
    ProductionResponseRO getProduction(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey, @QueryParam("start_date") String start, @QueryParam("end_date") String end);
}
