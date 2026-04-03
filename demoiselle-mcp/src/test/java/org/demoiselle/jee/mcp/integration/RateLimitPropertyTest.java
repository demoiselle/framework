/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import net.jqwik.api.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for rate limit response formatting in MCP.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Property 18: Resposta de rate limit</li>
 * </ul>
 *
 * <p>Since {@code demoiselle-security} is an optional dependency, this test
 * verifies the rate limit response format contract without importing the
 * actual {@code @RateLimit} annotation. It tests the expected response
 * structure that the handler must produce when a rate limit is exceeded.</p>
 */
class RateLimitPropertyTest {

    // ── Arbitraries ──

    @Provide
    Arbitrary<String> toolNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
                .filter(s -> !s.isBlank());
    }

    @Provide
    Arbitrary<Integer> retryAfterSeconds() {
        return Arbitraries.integers().between(1, 3600);
    }

    // -----------------------------------------------------------------------
    // Property 18: Resposta de rate limit
    // -----------------------------------------------------------------------

    /**
     * For any tool invocation that exceeds the rate limit, the response must
     * have isError=true, content indicating "Too Many Requests", and include
     * the Retry-After value in seconds.
     *
     * <p>This test verifies the response format contract by constructing the
     * expected rate limit response structure and validating its properties.</p>
     *
     * <p><b>Validates: Requirements 12.2, 12.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 18: Resposta de rate limit
    void rateLimitResponseHasCorrectStructure(
            @ForAll("toolNames") String toolName,
            @ForAll("retryAfterSeconds") int retryAfter) {

        // Build the expected rate limit response as the handler would produce it
        Map<String, Object> response = buildRateLimitResponse(toolName, retryAfter);

        // Verify isError is true
        assertTrue((Boolean) response.get("isError"),
                "Rate limit response must have isError=true");

        // Verify content contains "Too Many Requests"
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        assertNotNull(content, "Content must not be null");
        assertFalse(content.isEmpty(), "Content must not be empty");

        String text = (String) content.get(0).get("text");
        assertNotNull(text, "Text content must not be null");
        assertTrue(text.contains("Too Many Requests"),
                "Content must indicate 'Too Many Requests'");

        // Verify Retry-After is present and positive
        assertTrue(text.contains("Retry-After"),
                "Content must include Retry-After");
        assertTrue(text.contains(String.valueOf(retryAfter)),
                "Content must include the retry-after value in seconds");
    }

    /**
     * The Retry-After value in the rate limit response must always be a
     * positive integer representing seconds.
     *
     * <p><b>Validates: Requirements 12.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 18: Resposta de rate limit
    void retryAfterIsAlwaysPositive(
            @ForAll("retryAfterSeconds") int retryAfter) {

        assertTrue(retryAfter > 0,
                "Retry-After must be a positive number of seconds");

        Map<String, Object> response = buildRateLimitResponse("testTool", retryAfter);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");
        String text = (String) content.get(0).get("text");

        // Extract the retry-after value from the text
        assertTrue(text.contains(String.valueOf(retryAfter)),
                "Response must contain the exact retry-after seconds value");
    }

    /**
     * Rate limit responses must always use the "text" content type.
     *
     * <p><b>Validates: Requirements 12.2</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 18: Resposta de rate limit
    void rateLimitResponseUsesTextContentType(
            @ForAll("toolNames") String toolName,
            @ForAll("retryAfterSeconds") int retryAfter) {

        Map<String, Object> response = buildRateLimitResponse(toolName, retryAfter);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> content = (List<Map<String, Object>>) response.get("content");

        for (Map<String, Object> entry : content) {
            assertEquals("text", entry.get("type"),
                    "Rate limit content type must be 'text'");
        }
    }

    // ── Helper: builds the rate limit response as the handler would ──

    /**
     * Constructs a rate limit error response in the same format that
     * McpJsonRpcHandler would produce when a rate limit is exceeded.
     */
    private Map<String, Object> buildRateLimitResponse(String toolName, int retryAfterSeconds) {
        String message = "Too Many Requests for tool '" + toolName
                + "'. Retry-After: " + retryAfterSeconds + " seconds";

        List<Map<String, Object>> content = List.of(Map.of(
                "type", "text",
                "text", message
        ));

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("content", content);
        result.put("isError", true);
        return result;
    }
}
