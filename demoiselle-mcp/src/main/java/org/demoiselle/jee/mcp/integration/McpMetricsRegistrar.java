/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Registers and manages MCP tool invocation and error counters.
 *
 * <p>This class is registered conditionally when {@code demoiselle-observability}
 * is on the classpath. It provides simple counter-based metrics tracking using
 * {@link ConcurrentHashMap} and {@link AtomicLong}, avoiding a hard compile-time
 * dependency on the optional observability module.</p>
 *
 * <p>For each registered tool, two counters are maintained:</p>
 * <ul>
 *   <li>{@code demoiselle.mcp.tools.<name>.invocations} — incremented on every call</li>
 *   <li>{@code demoiselle.mcp.tools.<name>.errors} — incremented when a call results in error</li>
 * </ul>
 */
public class McpMetricsRegistrar {

    private static final String PREFIX = "demoiselle.mcp.tools.";
    private static final String INVOCATIONS_SUFFIX = ".invocations";
    private static final String ERRORS_SUFFIX = ".errors";

    private final ConcurrentHashMap<String, AtomicLong> counters = new ConcurrentHashMap<>();

    /**
     * Registers counters for a tool. Creates both invocation and error counters
     * initialized to zero.
     *
     * @param toolName the name of the MCP tool
     * @throws NullPointerException if {@code toolName} is {@code null}
     */
    public void registerTool(String toolName) {
        if (toolName == null) {
            throw new NullPointerException("toolName must not be null");
        }
        counters.putIfAbsent(invocationKey(toolName), new AtomicLong(0));
        counters.putIfAbsent(errorKey(toolName), new AtomicLong(0));
    }

    /**
     * Records a successful invocation of the given tool.
     *
     * @param toolName the name of the MCP tool
     */
    public void recordInvocation(String toolName) {
        AtomicLong counter = counters.get(invocationKey(toolName));
        if (counter != null) {
            counter.incrementAndGet();
        }
    }

    /**
     * Records an error during invocation of the given tool.
     * Also increments the invocation counter.
     *
     * @param toolName the name of the MCP tool
     */
    public void recordError(String toolName) {
        recordInvocation(toolName);
        AtomicLong counter = counters.get(errorKey(toolName));
        if (counter != null) {
            counter.incrementAndGet();
        }
    }

    /**
     * Returns the current invocation count for the given tool.
     *
     * @param toolName the name of the MCP tool
     * @return the invocation count, or {@code -1} if the tool is not registered
     */
    public long getInvocationCount(String toolName) {
        AtomicLong counter = counters.get(invocationKey(toolName));
        return counter != null ? counter.get() : -1;
    }

    /**
     * Returns the current error count for the given tool.
     *
     * @param toolName the name of the MCP tool
     * @return the error count, or {@code -1} if the tool is not registered
     */
    public long getErrorCount(String toolName) {
        AtomicLong counter = counters.get(errorKey(toolName));
        return counter != null ? counter.get() : -1;
    }

    /**
     * Returns all registered counter names.
     *
     * @return an unmodifiable set of counter names
     */
    public Set<String> getCounterNames() {
        return Set.copyOf(counters.keySet());
    }

    /**
     * Returns a snapshot of all counters as an unmodifiable map.
     *
     * @return map of counter name → current value
     */
    public Map<String, Long> snapshot() {
        var snap = new ConcurrentHashMap<String, Long>();
        counters.forEach((key, value) -> snap.put(key, value.get()));
        return Map.copyOf(snap);
    }

    // ── Key helpers ──

    private String invocationKey(String toolName) {
        return PREFIX + toolName + INVOCATIONS_SUFFIX;
    }

    private String errorKey(String toolName) {
        return PREFIX + toolName + ERRORS_SUFFIX;
    }
}
