package de.hatoka.eos.hardware.enphase.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Token from authentication uri
 * example:
 "access_token" : "eyJhbGciOiJSUzI1Ni",
 "token_type" : "bearer",
 "refresh_token" : "eyJhbGciOiJSUzI1Ni",
 "expires_in" : 86399,
 "scope" : "read write",
 "enl_uid" : "4990367",
 "enl_cid" : "",
 "enl_password_last_changed" : "1738061141",
 "is_internal_app" : false,
 "app_type" : "system",
 "app_Id" : "1409625451288",
 "jti" : "3fa8c964-1636-4710-aea3-44f1b2d49e2f"
 **/
@JsonIgnoreProperties(ignoreUnknown = true)
public class EnphaseToken
{
    @JsonProperty("access_token")
    private String access;
    @JsonProperty("token_type")
    private String type;
    @JsonProperty("refresh_token")
    private String refresh;
    @JsonProperty("expires_in")
    private Integer expiresIn;

    public String getRefresh() {
        return refresh;
    }

    public String getType()
    {
        return type;
    }

    public String getAccess()
    {
        return access;
    }
}
