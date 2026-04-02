/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 7: Media type application/problem+json no formato RFC 9457

import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.message.DemoiselleRESTMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test verifying that the media type of the {@link Response}
 * returned by {@link ExceptionTreatmentImpl#getFormatedErrorRfc9457} is always
 * {@code application/problem+json} for any arbitrary exception.
 *
 * <p><b>Validates: Requirements 3.1</b></p>
 */
class ExceptionMediaTypePropertyTest {

    static {
        TestRuntimeDelegate.install();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private ExceptionTreatmentImpl createInstance() throws Exception {
        ExceptionTreatmentImpl impl = new ExceptionTreatmentImpl();

        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setErrorFormat("rfc9457");
        config.setShowErrorDetails(true);
        setField(impl, "config", config);

        DemoiselleRESTMessage messages = mock(DemoiselleRESTMessage.class);
        setField(impl, "messages", messages);

        return impl;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private HttpServletRequest mockRequest() {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/test");
        return request;
    }

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<String> nonEmptyStrings() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    // ── Property 7: Media type application/problem+json ────────────

    /**
     * For any DemoiselleRestException with a random statusCode (100-599)
     * and at least one message, calling getFormatedErrorRfc9457 must produce
     * a Response whose media type is {@code application/problem+json}.
     *
     * <p><b>Validates: Requirements 3.1</b></p>
     */
    @Property(tries = 100)
    void responseMediaTypeIsApplicationProblemJson(
            @ForAll @IntRange(min = 100, max = 599) int statusCode,
            @ForAll("nonEmptyStrings") String error,
            @ForAll("nonEmptyStrings") String description
    ) throws Exception {
        // Build exception with the given statusCode and one message
        DemoiselleRestException exception = new DemoiselleRestException(statusCode);
        exception.addMessage(error, description, null);

        ExceptionTreatmentImpl impl = createInstance();
        Response response = impl.getFormatedErrorRfc9457(exception, mockRequest());

        // The core property: media type must be application/problem+json
        assertNotNull(response.getMediaType(), "Response media type must not be null");
        assertEquals("application/problem+json", response.getMediaType().toString(),
                "Response media type must be application/problem+json but was "
                        + response.getMediaType().toString());
    }
}
