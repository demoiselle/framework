/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 4: Round-trip de mapeamento DemoiselleRestExceptionMessage → ProblemDetail

import java.lang.reflect.Field;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.rest.DemoiselleRestConfig;
import org.demoiselle.jee.rest.exception.DemoiselleRestException;
import org.demoiselle.jee.rest.exception.DemoiselleRestExceptionMessage;
import org.demoiselle.jee.rest.message.DemoiselleRESTMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based test for round-trip mapping of
 * DemoiselleRestExceptionMessage → ProblemDetail.
 *
 * <p>For any {@code DemoiselleRestExceptionMessage} with non-empty {@code errorLink},
 * mapping to ProblemDetail and extracting back must preserve {@code error},
 * {@code errorDescription} and {@code errorLink}.</p>
 *
 * <p><b>Validates: Requirements 2.1, 2.2, 2.3, 2.5</b></p>
 */
class ExceptionMessageMappingRoundTripPropertyTest {

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
    Arbitrary<String> nonEmptyErrors() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
    }

    @Provide
    Arbitrary<String> optionalDescriptions() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(50)
                .injectNull(0.3);
    }

    @Provide
    Arbitrary<String> nonEmptyErrorLinks() {
        return Arbitraries.of(
                "https://example.com/errors/not-found",
                "urn:demoiselle:validation-error",
                "https://api.example.org/problems/conflict",
                "urn:ietf:rfc:9457",
                "https://docs.example.com/err/42"
        );
    }

    // ── Property 4: Round-trip mapping ─────────────────────────────

    /**
     * For any DemoiselleRestExceptionMessage with non-empty errorLink,
     * mapping to ProblemDetail via mapToProblemDetail and extracting back
     * must preserve error → title, errorDescription → detail, and
     * errorLink → type.
     *
     * <p><b>Validates: Requirements 2.1, 2.2, 2.3, 2.5</b></p>
     */
    @Property(tries = 100)
    void roundTripMappingPreservesFields(
            @ForAll("nonEmptyErrors") String error,
            @ForAll("optionalDescriptions") String errorDescription,
            @ForAll("nonEmptyErrorLinks") String errorLink,
            @ForAll @IntRange(min = 100, max = 599) int statusCode
    ) throws Exception {
        // Build a DemoiselleRestExceptionMessage with non-empty errorLink
        DemoiselleRestExceptionMessage msg =
                new DemoiselleRestExceptionMessage(error, errorDescription, errorLink);

        // Create a DemoiselleRestException with that single message
        DemoiselleRestException exception = new DemoiselleRestException(statusCode);
        exception.addMessage(msg.error(), msg.errorDescription(), msg.errorLink());

        // Map to ProblemDetail using the package-private method
        ExceptionTreatmentImpl impl = createInstance();
        ProblemDetail pd = impl.mapToProblemDetail(exception, true);

        // Extract back and verify round-trip preservation
        // Req 2.1: error → title
        assertEquals(msg.error(), pd.getTitle(),
                "error must map to ProblemDetail title");

        // Req 2.2: errorDescription → detail
        assertEquals(msg.errorDescription(), pd.getDetail(),
                "errorDescription must map to ProblemDetail detail");

        // Req 2.3: non-empty errorLink → type
        assertEquals(msg.errorLink(), pd.getType(),
                "non-empty errorLink must map to ProblemDetail type");
    }
}
