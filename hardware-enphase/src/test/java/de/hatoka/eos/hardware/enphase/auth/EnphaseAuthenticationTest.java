package de.hatoka.eos.hardware.enphase.auth;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class EnphaseAuthenticationTest
{
    @Inject
    EnphaseAuthentication authentication;

    @Test
    void testAuthentication() throws IOException
    {
        EnphaseToken token = authentication.getToken();
        assertNotNull(token);
        assertEquals("bearer", token.getType());
    }
}
