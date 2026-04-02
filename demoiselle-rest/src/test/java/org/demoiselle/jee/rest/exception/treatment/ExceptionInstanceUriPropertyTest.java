/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 9: Instance preenchido com URI da requisição

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
 * Property-based test verifying that the {@code instance} field of the
 * {@link ProblemDetail} body always equals the request URI from the
 * {@link HttpServletRequest} when the request is non-null.
 *
 * <p><b>Validates: Requirement 3.8</b></p>
 */
class ExceptionInstanceUriPropertyTest {

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

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<String> nonEmptyStrings() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    @Provide
    Arbitrary<String> requestUris() {
        Arbitrary<String> segments = Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(15);
        Arbitrary<Integer> numericIds = Arbitraries.integers().between(1, 9999);

        return Arbitraries.oneOf(
                // Simple paths like /api/users
                segments.map(s -> "/api/" + s),
                // Paths with numeric id like /api/items/42
                Combinators.combine(segments, numericIds)
                        .as((s, id) -> "/api/" + s + "/" + id),
                // Deeper paths like /api/v2/resources
                Combinators.combine(segments, segments)
                        .as((s1, s2) -> "/api/" + s1 + "/" + s2),
                // Root path
                Arbitraries.just("/")
        );
    }

    // ── Property 9: Instance URI ───────────────────────────────────

    /**
     * For any DemoiselleRestException with a random statusCode and messages,
     * and any non-null HttpServletRequest returning a generated URI,
     * calling getFormatedErrorRfc9457 must produce a ProblemDetail whose
     * {@code instance} field equals the request URI.
     *
     * <p><b>Validates: Requirement 3.8</b></p>
     */
    @Property(tries = 100)
    void instanceFieldEqualsRequestUri(
            @ForAll @IntRange(min = 100, max = 599) int statusCode,
            @ForAll("nonEmptyStrings") String error,
            @ForAll("nonEmptyStrings") String description,
            @ForAll("requestUris") String requestUri
    ) throws Exception {
        // Build exception with the given statusCode and one message
        DemoiselleRestException exception = new DemoiselleRestException(statusCode);
        exception.addMessage(error, description, null);

        // Mock HttpServletRequest to return the generated URI
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        when(mockRequest.getRequestURI()).thenReturn(requestUri);

        ExceptionTreatmentImpl impl = createInstance();
        Response response = impl.getFormatedErrorRfc9457(exception, mockRequest);

        // Extract ProblemDetail from the response entity
        assertNotNull(response.getEntity(), "Response entity must not be null");
        assertInstanceOf(ProblemDetail.class, response.getEntity(),
                "Response entity must be a ProblemDetail");

        ProblemDetail pd = (ProblemDetail) response.getEntity();

        // The core property: instance must equal the request URI
        assertEquals(requestUri, pd.getInstance(),
                "ProblemDetail instance (" + pd.getInstance()
                        + ") must equal request URI (" + requestUri + ")");
    }
}
