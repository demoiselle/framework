/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.lang.reflect.Field;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.core.message.DemoiselleMessage;
import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link RestFilter}.
 *
 * @author SERPRO
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RestFilterTest {

    @Mock ContainerRequestContext requestContext;
    @Mock ContainerResponseContext responseContext;
    @Mock DemoiselleMessage demoiselleMessage;

    MultivaluedMap<String, Object> headers;
    RestFilter filter;
    DemoiselleRestConfig config;

    @BeforeEach
    void setUp() throws Exception {
        headers = new MultivaluedHashMap<>();
        when(responseContext.getHeaders()).thenReturn(headers);
        when(demoiselleMessage.frameworkName()).thenReturn("Demoiselle 4");

        config = new DemoiselleRestConfig();
        filter = new RestFilter();
        inject(filter, "demoiselleMessage", demoiselleMessage);
        inject(filter, "config", config);
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }

    @Test
    void defaultConfigAppliesSecurityHeadersAndHidesVersion() {
        filter.filter(requestContext, responseContext);

        assertFalse(headers.containsKey("Demoiselle-Version"));
        assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"));
        assertEquals("DENY", headers.getFirst("X-Frame-Options"));
        assertEquals("no-referrer", headers.getFirst("Referrer-Policy"));
        assertEquals("camera=(), microphone=(), geolocation=()", headers.getFirst("Permissions-Policy"));
    }

    @Test
    void exposeFrameworkVersionWhenEnabled() {
        config.setExposeFrameworkVersion(true);

        filter.filter(requestContext, responseContext);

        assertEquals("Demoiselle 4", headers.getFirst("Demoiselle-Version"));
    }

    @Test
    void doesNotOverrideExistingFrameworkVersionHeader() {
        config.setExposeFrameworkVersion(true);
        headers.putSingle("Demoiselle-Version", "application-managed");

        filter.filter(requestContext, responseContext);

        assertEquals("application-managed", headers.getFirst("Demoiselle-Version"));
    }

    @Test
    void securityHeadersDisabledDoesNotAddHeaders() {
        config.setSecurityHeadersEnabled(false);

        filter.filter(requestContext, responseContext);

        assertFalse(headers.containsKey("X-Content-Type-Options"));
        assertFalse(headers.containsKey("X-Frame-Options"));
    }

    @Test
    void doesNotOverrideExistingHeader() {
        headers.putSingle("X-Frame-Options", "SAMEORIGIN");

        filter.filter(requestContext, responseContext);

        assertEquals("SAMEORIGIN", headers.getFirst("X-Frame-Options"));
        // other defaults still applied
        assertEquals("nosniff", headers.getFirst("X-Content-Type-Options"));
    }

    @Test
    void noHstsOrCspApplied() {
        filter.filter(requestContext, responseContext);

        assertFalse(headers.containsKey("Strict-Transport-Security"));
        assertFalse(headers.containsKey("Content-Security-Policy"));
    }
}
