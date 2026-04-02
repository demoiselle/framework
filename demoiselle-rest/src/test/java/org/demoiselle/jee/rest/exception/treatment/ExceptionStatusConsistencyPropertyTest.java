/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 6: Invariante status-consistente nas respostas RFC 9457

import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.Response;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.Size;

import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.message.DemoiselleRESTMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test verifying that the {@code status} field inside the
 * {@link ProblemDetail} body always equals the HTTP status code of the
 * JAX-RS {@link Response} returned by
 * {@link ExceptionTreatmentImpl#getFormatedErrorRfc9457}.
 *
 * <p><b>Validates: Requirements 3.9, 3.5</b></p>
 */
class ExceptionStatusConsistencyPropertyTest {

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

    // ── Property 6: Status consistency ─────────────────────────────

    /**
     * For any DemoiselleRestException with a random statusCode (100-599)
     * and at least one message, calling getFormatedErrorRfc9457 must produce
     * a Response whose HTTP status equals the ProblemDetail's status field.
     *
     * <p><b>Validates: Requirements 3.9, 3.5</b></p>
     */
    @Property(tries = 100)
    void problemDetailStatusMatchesResponseStatus(
            @ForAll @IntRange(min = 100, max = 599) int statusCode,
            @ForAll("nonEmptyStrings") String error,
            @ForAll("nonEmptyStrings") String description
    ) throws Exception {
        // Build exception with the given statusCode and one message
        DemoiselleRestException exception = new DemoiselleRestException(statusCode);
        exception.addMessage(error, description, null);

        ExceptionTreatmentImpl impl = createInstance();
        Response response = impl.getFormatedErrorRfc9457(exception, mockRequest());

        // Extract ProblemDetail from the response entity
        assertNotNull(response.getEntity(), "Response entity must not be null");
        assertInstanceOf(ProblemDetail.class, response.getEntity(),
                "Response entity must be a ProblemDetail");

        ProblemDetail pd = (ProblemDetail) response.getEntity();

        // The core invariant: ProblemDetail.status == Response HTTP status
        assertEquals(pd.getStatus(), response.getStatus(),
                "ProblemDetail status (" + pd.getStatus()
                        + ") must equal Response HTTP status (" + response.getStatus() + ")");
    }
}
