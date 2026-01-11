package de.hatoka.eos.service;

import io.quarkus.runtime.Quarkus;
import io.quarkus.runtime.QuarkusApplication;
import io.quarkus.runtime.annotations.QuarkusMain;
import jakarta.inject.Inject;

@QuarkusMain
public class EosApplication implements QuarkusApplication
{
    @Inject
    private SimulationNow simulation;
    @Inject
    private ForecastImport forecast;
    @Inject
    private TelemetryImport telemetryImport;

    public static void main(String[] args)
    {
        Quarkus.run(EosApplication.class, args);
    }

    @Override
    public int run(String... args) throws Exception
    {
        // forecast.run();
        // simulation.run();
        telemetryImport.run();
        return 0;
    }
}