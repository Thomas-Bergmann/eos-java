package de.hatoka.eos.hardware.enphase.auth;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@QuarkusTest
public class EnphaseAuthenticationTest
{
    private static final Logger LOGGER = LoggerFactory.getLogger(EnphaseAuthenticationTest.class);

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
