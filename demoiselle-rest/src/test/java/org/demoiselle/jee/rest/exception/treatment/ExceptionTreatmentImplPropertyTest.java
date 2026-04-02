/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import java.lang.reflect.Field;
import java.sql.SQLException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.ClientErrorException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import net.jqwik.api.*;

import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.message.DemoiselleRESTMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for {@link ExceptionTreatmentImpl} RFC 9457 support.
 *
 * <p><b>Validates: Requirements 4.1, 4.2, 4.5, 4.7, 4.8</b></p>
 */
class ExceptionTreatmentImplPropertyTest {

    static {
        TestRuntimeDelegate.install();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private ExceptionTreatmentImpl createInstance(boolean rfc9457, boolean showErrorDetails) throws Exception {
        ExceptionTreatmentImpl impl = new ExceptionTreatmentImpl();

        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setErrorFormat(rfc9457 ? "rfc9457" : "legacy");
        config.setShowErrorDetails(showErrorDetails);
        setField(impl, "config", config);

        // Legacy mode needs the messages field for error text
        DemoiselleRESTMessage messages = mock(DemoiselleRESTMessage.class);
        when(messages.unhandledServerException()).thenReturn("Internal Server Error");
        when(messages.unhandledDatabaseException()).thenReturn("Database Error");
        when(messages.unhandledMalformedInputOutputException()).thenReturn("Malformed Input");
        when(messages.httpException()).thenReturn("HTTP Error");
        setField(impl, "messages", messages);

        return impl;
    }

    private HttpServletRequest mockRequest(String uri) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn(uri);
        when(request.getHeader("content-type")).thenReturn(null);
        return request;
    }

    private static void setField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<Throwable> anyExceptions() {
        return Arbitraries.oneOf(
                Arbitraries.create(() -> new RuntimeException("generic error")),
                Arbitraries.create(() -> new SQLException("db error", "42000", 1234)),
                Arbitraries.create(() -> new DemoiselleRestException("demoiselle error", 422)),
                Arbitraries.create(() -> new DemoiselleRestException("bad request", 400)),
                Arbitraries.create(() -> new ClientErrorException("not found", 404)),
                Arbitraries.create(() -> new RuntimeException("wrapped",
                        new DemoiselleRestException("inner", 409)))
        );
    }

    @Provide
    Arbitrary<String> requestUris() {
        return Arbitraries.of(
                "/api/users",
                "/api/orders/123",
                "/health",
                "/api/v2/items"
        );
    }

    @Provide
    Arbitrary<Boolean> booleans() {
        return Arbitraries.of(true, false);
    }

    // ── Property 5: Response format matches config ─────────────────

    // Feature: rest-enhancements, Property 5: Formato de resposta corresponde à configuração errorFormat
    /**
     * For any exception and any errorFormat config, when errorFormat is "rfc9457"
     * the media type must be "application/problem+json", when "legacy" it must be
     * "application/json".
     *
     * <p><b>Validates: Requirements 4.1, 4.2</b></p>
     */
    @Property(tries = 100)
    void responseFormatMatchesConfig(
            @ForAll("anyExceptions") Throwable exception,
            @ForAll("booleans") boolean rfc9457,
            @ForAll("requestUris") String uri
    ) throws Exception {
        ExceptionTreatmentImpl impl = createInstance(rfc9457, true);
        HttpServletRequest request = mockRequest(uri);

        Response response = impl.getFormatedError(exception, request);

        MediaType mediaType = response.getMediaType();
        if (rfc9457) {
            assertEquals("application", mediaType.getType(),
                    "RFC 9457 mode must use application/problem+json type");
            assertEquals("problem+json", mediaType.getSubtype(),
                    "RFC 9457 mode must use application/problem+json subtype");
        } else {
            // Legacy mode uses application/json (since content-type header is null)
            assertEquals("application", mediaType.getType(),
                    "Legacy mode must use application/json type");
            assertEquals("json", mediaType.getSubtype(),
                    "Legacy mode must use application/json subtype");
        }
    }

    // ── Property 6: DemoiselleRestException mapping ────────────────

    // Feature: rest-enhancements, Property 6: DemoiselleRestException mapeada corretamente para ProblemDetail
    /**
     * For any DemoiselleRestException with arbitrary statusCode and message,
     * when format is "rfc9457", the ProblemDetail must have status equal to the
     * exception's statusCode and detail containing the exception message
     * (when showErrorDetails is true).
     *
     * <p><b>Validates: Requirement 4.5</b></p>
     */
    @Property(tries = 100)
    void demoiselleRestExceptionMappedCorrectly(
            @ForAll @net.jqwik.api.constraints.IntRange(min = 100, max = 599) int statusCode,
            @ForAll("requestUris") String uri
    ) throws Exception {
        String message = "error-" + statusCode;
        DemoiselleRestException exception = new DemoiselleRestException(message, statusCode);

        ExceptionTreatmentImpl impl = createInstance(true, true);
        HttpServletRequest request = mockRequest(uri);

        Response response = impl.getFormatedError(exception, request);

        assertEquals(statusCode, response.getStatus(),
                "Response status must match exception statusCode");

        // Read entity as ProblemDetail
        ProblemDetail pd = (ProblemDetail) response.getEntity();
        assertEquals(statusCode, pd.getStatus(),
                "ProblemDetail status must match exception statusCode");
        assertEquals(message, pd.getDetail(),
                "ProblemDetail detail must contain exception message when showErrorDetails is true");
    }

    // ── Property 7: RFC 9457 structural invariants ─────────────────

    // Feature: rest-enhancements, Property 7: Invariantes estruturais da resposta RFC 9457
    /**
     * For any exception in "rfc9457" mode:
     * (a) instance equals the request URI;
     * (b) when showErrorDetails is false, detail is null.
     *
     * <p><b>Validates: Requirements 4.7, 4.8</b></p>
     */
    @Property(tries = 100)
    void rfc9457StructuralInvariants(
            @ForAll("anyExceptions") Throwable exception,
            @ForAll("booleans") boolean showErrorDetails,
            @ForAll("requestUris") String uri
    ) throws Exception {
        ExceptionTreatmentImpl impl = createInstance(true, showErrorDetails);
        HttpServletRequest request = mockRequest(uri);

        Response response = impl.getFormatedError(exception, request);
        ProblemDetail pd = (ProblemDetail) response.getEntity();

        // (a) instance equals request URI
        assertEquals(uri, pd.getInstance(),
                "ProblemDetail instance must equal request URI");

        // (b) when showErrorDetails is false, detail must be null
        if (!showErrorDetails) {
            assertNull(pd.getDetail(),
                    "ProblemDetail detail must be null when showErrorDetails is false");
        }
    }
}
