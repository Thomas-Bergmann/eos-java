package de.hatoka.eos.hardware.enphase.clients;

import de.hatoka.eos.hardware.enphase.model.*;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

@RegisterRestClient
public interface EnphaseSiteLevelProductionMonitoringClient
{
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/telemetry/battery")
    TelemetryBatteryResponseRO getBatteryTelemetry(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey, @QueryParam("start_at") long start, @QueryParam("end_at") long end, @QueryParam("granularity") String granularity);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/telemetry/consumption_meter")
    TelemetryConsumptionResponseRO getConsumptionTelemetry(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey, @QueryParam("start_at") long start, @QueryParam("end_at") long end, @QueryParam("granularity") String granularity);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/energy_import_telemetry")
    TelemetryImportResponseRO getImportTelemetry(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey, @QueryParam("start_at") long start, @QueryParam("end_at") long end, @QueryParam("granularity") String granularity);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/energy_export_telemetry")
    TelemetryExportResponseRO getExportTelemetry(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey, @QueryParam("start_at") long start, @QueryParam("end_at") long end, @QueryParam("granularity") String granularity);

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/api/v4/systems/{system_id}/telemetry/production_meter")
    TelemetryProductionResponseRO getProductionTelemetry(@HeaderParam("Authorization") String authorization, @PathParam("system_id") String systemId,
                    @QueryParam("key") String apiKey, @QueryParam("start_at") long start, @QueryParam("end_at") long end, @QueryParam("granularity") String granularity);
}
