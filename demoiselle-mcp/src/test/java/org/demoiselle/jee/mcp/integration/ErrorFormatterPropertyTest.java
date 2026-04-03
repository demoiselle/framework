/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import net.jqwik.api.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for error formatting in the MCP integration layer.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Property 15: Conditional error formatting based on classpath
 *       (ProblemDetail vs plain text)</li>
 *   <li>Property 16: Exception type to ProblemDetail status mapping
 *       (validation → 422, generic → 500)</li>
 * </ul>
 */
class ErrorFormatterPropertyTest {

    // -----------------------------------------------------------------------
    // Custom Arbitraries
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<String> errorMessages() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(200)
                .filter(s -> !s.isBlank());
    }

    @Provide
    Arbitrary<Throwable> validationExceptions() {
        return Arbitraries.of(
                new IllegalArgumentException("invalid input"),
                new IllegalArgumentException("bad parameter value"),
                new IllegalArgumentException("constraint violation")
        );
    }

    @Provide
    Arbitrary<Throwable> genericExceptions() {
        return Arbitraries.of(
                new RuntimeException("something went wrong"),
                new NullPointerException("null reference"),
                new UnsupportedOperationException("not supported"),
                new IllegalStateException("bad state"),
                new ArithmeticException("division by zero")
        );
    }

    @Provide
    Arbitrary<Throwable> allExceptions() {
        return Arbitraries.oneOf(validationExceptions(), genericExceptions());
    }

    // -----------------------------------------------------------------------
    // Property 15: Formatação de erros condicional ao classpath
    // -----------------------------------------------------------------------

    /**
     * When demoiselle-rest is available, ProblemDetailErrorFormatter produces
     * a response containing ProblemDetail fields (type, title, status, detail).
     * When demoiselle-rest is NOT available, PlainTextErrorFormatter produces
     * a plain text response with just the exception message.
     *
     * <p>We test each formatter directly to verify the contract.</p>
     *
     * <p><b>Validates: Requirements 10.1, 10.2, 19.1</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 15: Formatação de erros condicional ao classpath
    void problemDetailFormatterProducesProblemDetailContent(
            @ForAll("allExceptions") Throwable exception) {

        ProblemDetailErrorFormatter formatter = new ProblemDetailErrorFormatter();
        List<Map<String, Object>> result = formatter.formatError(exception);

        assertNotNull(result, "Result must not be null");
        assertEquals(1, result.size(), "Result must contain exactly one content entry");

        Map<String, Object> content = result.get(0);
        assertEquals("text", content.get("type"), "Content type must be 'text'");

        String text = (String) content.get("text");
        assertNotNull(text, "Text content must not be null");

        // ProblemDetail formatted output must contain key fields
        assertTrue(text.contains("title"), "ProblemDetail text must contain 'title'");
        assertTrue(text.contains("status"), "ProblemDetail text must contain 'status'");
        assertTrue(text.contains("detail"), "ProblemDetail text must contain 'detail'");
    }

    /**
     * PlainTextErrorFormatter produces a simple text response with the
     * exception message — no ProblemDetail structure.
     *
     * <p><b>Validates: Requirements 10.2, 19.1</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 15: Formatação de erros condicional ao classpath
    void plainTextFormatterProducesSimpleTextContent(
            @ForAll("allExceptions") Throwable exception) {

        PlainTextErrorFormatter formatter = new PlainTextErrorFormatter();
        List<Map<String, Object>> result = formatter.formatError(exception);

        assertNotNull(result, "Result must not be null");
        assertEquals(1, result.size(), "Result must contain exactly one content entry");

        Map<String, Object> content = result.get(0);
        assertEquals("text", content.get("type"), "Content type must be 'text'");

        String text = (String) content.get("text");
        assertNotNull(text, "Text content must not be null");

        // Plain text should contain the exception message directly
        String expectedMessage = exception.getMessage() != null
                ? exception.getMessage()
                : exception.getClass().getSimpleName();
        assertEquals(expectedMessage, text,
                "Plain text formatter must return the exception message directly");
    }

    /**
     * The two formatters must produce structurally different outputs for the
     * same exception — ProblemDetail contains structured fields while plain
     * text is just the message.
     *
     * <p><b>Validates: Requirements 10.1, 10.2, 19.1</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 15: Formatação de erros condicional ao classpath
    void formattersProduceDifferentOutputForSameException(
            @ForAll("allExceptions") Throwable exception) {

        ProblemDetailErrorFormatter problemFormatter = new ProblemDetailErrorFormatter();
        PlainTextErrorFormatter plainFormatter = new PlainTextErrorFormatter();

        String problemText = (String) problemFormatter.formatError(exception).get(0).get("text");
        String plainText = (String) plainFormatter.formatError(exception).get(0).get("text");

        // ProblemDetail output is always longer/different from plain text
        assertNotEquals(problemText, plainText,
                "ProblemDetail and plain text formatters must produce different output");
    }

    // -----------------------------------------------------------------------
    // Property 16: Mapeamento de tipo de exceção para status ProblemDetail
    // -----------------------------------------------------------------------

    /**
     * For any validation exception (IllegalArgumentException), the
     * ProblemDetail must have status=422 and title="Validation Failed".
     *
     * <p><b>Validates: Requirements 10.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 16: Mapeamento de tipo de exceção para status ProblemDetail
    void validationExceptionMapsToStatus422(
            @ForAll("errorMessages") String message) {

        ProblemDetailErrorFormatter formatter = new ProblemDetailErrorFormatter();
        IllegalArgumentException exception = new IllegalArgumentException(message);

        Map<String, Object> problemDetail = formatter.toProblemDetailMap(exception);

        assertEquals(422, problemDetail.get("status"),
                "Validation exception must map to status 422");
        assertEquals("Validation Failed", problemDetail.get("title"),
                "Validation exception must have title 'Validation Failed'");
        assertEquals(message, problemDetail.get("detail"),
                "Detail must contain the exception message");
        assertEquals("about:blank", problemDetail.get("type"),
                "Type must be 'about:blank'");
    }

    /**
     * For any generic (non-validation) exception, the ProblemDetail must
     * have status=500 and title="Internal Server Error".
     *
     * <p><b>Validates: Requirements 10.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 16: Mapeamento de tipo de exceção para status ProblemDetail
    void genericExceptionMapsToStatus500(
            @ForAll("errorMessages") String message) {

        ProblemDetailErrorFormatter formatter = new ProblemDetailErrorFormatter();
        RuntimeException exception = new RuntimeException(message);

        Map<String, Object> problemDetail = formatter.toProblemDetailMap(exception);

        assertEquals(500, problemDetail.get("status"),
                "Generic exception must map to status 500");
        assertEquals("Internal Server Error", problemDetail.get("title"),
                "Generic exception must have title 'Internal Server Error'");
        assertEquals(message, problemDetail.get("detail"),
                "Detail must contain the exception message");
        assertEquals("about:blank", problemDetail.get("type"),
                "Type must be 'about:blank'");
    }

    /**
     * For any exception with a null message, the detail field must fall back
     * to the exception class simple name.
     *
     * <p><b>Validates: Requirements 10.3, 10.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 16: Mapeamento de tipo de exceção para status ProblemDetail
    void nullMessageFallsBackToClassName(
            @ForAll("genericExceptions") Throwable baseException) {

        // Create a new exception of the same type but with null message
        Throwable nullMsgException;
        if (baseException instanceof IllegalArgumentException) {
            nullMsgException = new IllegalArgumentException((String) null);
        } else if (baseException instanceof NullPointerException) {
            nullMsgException = new NullPointerException();
        } else if (baseException instanceof UnsupportedOperationException) {
            nullMsgException = new UnsupportedOperationException((String) null);
        } else if (baseException instanceof IllegalStateException) {
            nullMsgException = new IllegalStateException((String) null);
        } else {
            nullMsgException = new RuntimeException((String) null);
        }

        ProblemDetailErrorFormatter formatter = new ProblemDetailErrorFormatter();
        Map<String, Object> problemDetail = formatter.toProblemDetailMap(nullMsgException);

        assertEquals(nullMsgException.getClass().getSimpleName(), problemDetail.get("detail"),
                "When message is null, detail must be the exception class simple name");
    }
}
