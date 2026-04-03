/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import net.jqwik.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link McpMetricsRegistrar}.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Property 19: Contadores de métricas auto-registrados</li>
 * </ul>
 */
class MetricsPropertyTest {

    // ── Arbitraries ──

    @Provide
    Arbitrary<String> toolNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
                .filter(s -> !s.isBlank());
    }

    @Provide
    Arbitrary<Integer> invocationCounts() {
        return Arbitraries.integers().between(1, 50);
    }

    @Provide
    Arbitrary<Integer> errorCounts() {
        return Arbitraries.integers().between(0, 20);
    }

    // -----------------------------------------------------------------------
    // Property 19: Contadores de métricas auto-registrados
    // -----------------------------------------------------------------------

    /**
     * For every registered tool, both invocation and error counters must exist
     * with the correct naming convention.
     *
     * <p><b>Validates: Requirements 13.2, 13.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 19: Contadores de métricas auto-registrados
    void registeredToolHasBothCounters(
            @ForAll("toolNames") String toolName) {

        McpMetricsRegistrar registrar = new McpMetricsRegistrar();
        registrar.registerTool(toolName);

        Set<String> counterNames = registrar.getCounterNames();

        String expectedInvocations = "demoiselle.mcp.tools." + toolName + ".invocations";
        String expectedErrors = "demoiselle.mcp.tools." + toolName + ".errors";

        assertTrue(counterNames.contains(expectedInvocations),
                "Invocation counter must exist: " + expectedInvocations);
        assertTrue(counterNames.contains(expectedErrors),
                "Error counter must exist: " + expectedErrors);

        // Both counters must start at zero
        assertEquals(0, registrar.getInvocationCount(toolName),
                "Invocation counter must start at 0");
        assertEquals(0, registrar.getErrorCount(toolName),
                "Error counter must start at 0");
    }

    /**
     * For every invocation recorded, the invocation counter must be incremented
     * by exactly one.
     *
     * <p><b>Validates: Requirements 13.2</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 19: Contadores de métricas auto-registrados
    void invocationCounterIncrementsCorrectly(
            @ForAll("toolNames") String toolName,
            @ForAll("invocationCounts") int count) {

        McpMetricsRegistrar registrar = new McpMetricsRegistrar();
        registrar.registerTool(toolName);

        for (int i = 0; i < count; i++) {
            registrar.recordInvocation(toolName);
        }

        assertEquals(count, registrar.getInvocationCount(toolName),
                "Invocation counter must equal the number of recordInvocation calls");
        assertEquals(0, registrar.getErrorCount(toolName),
                "Error counter must remain 0 when no errors recorded");
    }

    /**
     * For every error recorded, the error counter must be incremented by exactly
     * one, and the invocation counter must also be incremented (since an error
     * is also an invocation).
     *
     * <p><b>Validates: Requirements 13.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 19: Contadores de métricas auto-registrados
    void errorCounterIncrementsCorrectly(
            @ForAll("toolNames") String toolName,
            @ForAll("invocationCounts") int successCount,
            @ForAll("errorCounts") int errorCount) {

        McpMetricsRegistrar registrar = new McpMetricsRegistrar();
        registrar.registerTool(toolName);

        // Record successful invocations
        for (int i = 0; i < successCount; i++) {
            registrar.recordInvocation(toolName);
        }

        // Record error invocations (each also increments invocation counter)
        for (int i = 0; i < errorCount; i++) {
            registrar.recordError(toolName);
        }

        assertEquals(successCount + errorCount, registrar.getInvocationCount(toolName),
                "Invocation counter must include both successes and errors");
        assertEquals(errorCount, registrar.getErrorCount(toolName),
                "Error counter must equal the number of recordError calls");
    }

    /**
     * For an unregistered tool, counters must return -1 indicating the tool
     * is not tracked.
     *
     * <p><b>Validates: Requirements 13.2, 13.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 19: Contadores de métricas auto-registrados
    void unregisteredToolReturnsNegativeOne(
            @ForAll("toolNames") String toolName) {

        McpMetricsRegistrar registrar = new McpMetricsRegistrar();
        // Do NOT register the tool

        assertEquals(-1, registrar.getInvocationCount(toolName),
                "Unregistered tool invocation count must be -1");
        assertEquals(-1, registrar.getErrorCount(toolName),
                "Unregistered tool error count must be -1");
    }
}
