/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 8: Omissão de detail quando showErrorDetails é false

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
 * Property-based test verifying that when {@code showErrorDetails} is
 * {@code false} and the error format is RFC 9457, the {@code detail}
 * field of the resulting {@link ProblemDetail} is always {@code null}.
 *
 * <p><b>Validates: Requirements 3.7</b></p>
 */
class ExceptionDetailOmissionPropertyTest {

    static {
        TestRuntimeDelegate.install();
    }

    // ── Helpers ────────────────────────────────────────────────────

    private ExceptionTreatmentImpl createInstance() throws Exception {
        ExceptionTreatmentImpl impl = new ExceptionTreatmentImpl();

        DemoiselleRestConfig config = new DemoiselleRestConfig();
        config.setErrorFormat("rfc9457");
        config.setShowErrorDetails(false);
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

    // ── Property 8: Detail omission when showErrorDetails is false ─

    /**
     * For any DemoiselleRestException with a random statusCode (100-599)
     * and arbitrary messages, calling getFormatedErrorRfc9457 with
     * showErrorDetails=false must produce a ProblemDetail whose detail
     * field is null.
     *
     * <p><b>Validates: Requirements 3.7</b></p>
     */
    @Property(tries = 100)
    void detailIsNullWhenShowErrorDetailsIsFalse(
            @ForAll @IntRange(min = 100, max = 599) int statusCode,
            @ForAll("nonEmptyStrings") String error,
            @ForAll("nonEmptyStrings") String description
    ) throws Exception {
        DemoiselleRestException exception = new DemoiselleRestException(statusCode);
        exception.addMessage(error, description, null);

        ExceptionTreatmentImpl impl = createInstance();
        Response response = impl.getFormatedErrorRfc9457(exception, mockRequest());

        assertNotNull(response.getEntity(), "Response entity must not be null");
        assertInstanceOf(ProblemDetail.class, response.getEntity(),
                "Response entity must be a ProblemDetail");

        ProblemDetail pd = (ProblemDetail) response.getEntity();

        assertNull(pd.getDetail(),
                "ProblemDetail.detail must be null when showErrorDetails is false, "
                        + "but was: '" + pd.getDetail() + "' for statusCode=" + statusCode);
    }
}
