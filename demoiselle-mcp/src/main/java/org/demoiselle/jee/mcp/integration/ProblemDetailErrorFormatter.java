/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import jakarta.enterprise.inject.Vetoed;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link ErrorFormatter} implementation that produces ProblemDetail (RFC 9457)
 * formatted error responses as Map representations.
 *
 * <p>This formatter is NOT a CDI bean ({@code @Vetoed}) to avoid ambiguity with
 * {@link PlainTextErrorFormatter}. It is instantiated directly by the
 * {@code McpJsonRpcHandler} when {@code demoiselle-rest} is on the classpath.</p>
 *
 * <p>Mapping rules:</p>
 * <ul>
 *   <li>Validation exceptions ({@link IllegalArgumentException},
 *       {@code jakarta.validation.ValidationException}) → status 422, title "Validation Failed"</li>
 *   <li>All other exceptions → status 500, title "Internal Server Error"</li>
 * </ul>
 */
@Vetoed
public class ProblemDetailErrorFormatter implements ErrorFormatter {

    private static final String VALIDATION_EXCEPTION_CLASS = "jakarta.validation.ValidationException";

    @Override
    public List<Map<String, Object>> formatError(Throwable exception) {
        Map<String, Object> problemDetail = new LinkedHashMap<>();

        if (isValidationException(exception)) {
            problemDetail.put("type", "about:blank");
            problemDetail.put("title", "Validation Failed");
            problemDetail.put("status", 422);
        } else {
            problemDetail.put("type", "about:blank");
            problemDetail.put("title", "Internal Server Error");
            problemDetail.put("status", 500);
        }

        String detail = exception.getMessage() != null
                ? exception.getMessage()
                : exception.getClass().getSimpleName();
        problemDetail.put("detail", detail);

        return List.of(Map.of(
                "type", "text",
                "text", problemDetail.toString()
        ));
    }

    /**
     * Determines whether the given exception is a validation exception.
     *
     * <p>Checks for {@link IllegalArgumentException} directly and for
     * {@code jakarta.validation.ValidationException} by class name to avoid
     * a hard dependency on the Jakarta Validation API.</p>
     */
    private boolean isValidationException(Throwable exception) {
        if (exception instanceof IllegalArgumentException) {
            return true;
        }
        // Check by class name to avoid hard dependency on jakarta.validation
        Class<?> clazz = exception.getClass();
        while (clazz != null) {
            if (VALIDATION_EXCEPTION_CLASS.equals(clazz.getName())) {
                return true;
            }
            clazz = clazz.getSuperclass();
        }
        return false;
    }

    /**
     * Extracts the ProblemDetail map from the formatted error response.
     * Useful for testing and inspection.
     *
     * @param exception the exception to format
     * @return the ProblemDetail as a Map
     */
    public Map<String, Object> toProblemDetailMap(Throwable exception) {
        Map<String, Object> problemDetail = new LinkedHashMap<>();

        if (isValidationException(exception)) {
            problemDetail.put("type", "about:blank");
            problemDetail.put("title", "Validation Failed");
            problemDetail.put("status", 422);
        } else {
            problemDetail.put("type", "about:blank");
            problemDetail.put("title", "Internal Server Error");
            problemDetail.put("status", 500);
        }

        String detail = exception.getMessage() != null
                ? exception.getMessage()
                : exception.getClass().getSimpleName();
        problemDetail.put("detail", detail);

        return problemDetail;
    }
}
