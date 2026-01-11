package de.hatoka.eos.hardware.enphase.clients;

public enum Granularity
{
    WEEK("week"), DAY("day"), MIN15("15mins"), MIN05("5mins");

    private final String queryParameter;

    Granularity(String queryParameter)
    {
        this.queryParameter = queryParameter;
    }

    public String getQueryParameter()
    {
        return queryParameter;
    }
}
