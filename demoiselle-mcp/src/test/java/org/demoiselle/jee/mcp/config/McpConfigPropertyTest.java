/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.config;

import net.jqwik.api.*;

import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;
import org.demoiselle.jee.mcp.registry.McpToolRegistry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for McpConfig disabled tools filtering.
 *
 * <p>Covers Property 20: for any comma-separated configuration string of tool
 * names, parsing must produce the correct set, and disabled tools must be
 * removed from the registry.</p>
 */
class McpConfigPropertyTest {

    private static final Method DUMMY_METHOD;

    static {
        try {
            DUMMY_METHOD = String.class.getMethod("toString");
        } catch (NoSuchMethodException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // -----------------------------------------------------------------------
    // Custom Arbitraries
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<String> toolNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20);
    }

    @Provide
    Arbitrary<List<String>> uniqueToolNames() {
        return toolNames()
                .list()
                .ofMinSize(1)
                .ofMaxSize(15)
                .uniqueElements();
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private ToolDescriptor toolDescriptor(String name) {
        return new ToolDescriptor(name, "desc-" + name, Map.of(), this, DUMMY_METHOD);
    }

    /**
     * Builds a comma-separated string from the given names, injecting random
     * whitespace and empty segments to exercise the parser.
     */
    private String toCommaSeparated(List<String> names) {
        return names.stream()
                .map(n -> "  " + n + " ")
                .collect(Collectors.joining(","));
    }

    // -----------------------------------------------------------------------
    // Property 20: Filtragem de ferramentas desabilitadas
    // -----------------------------------------------------------------------

    /**
     * For any list of unique tool names joined as a comma-separated string,
     * {@code getDisabledToolNames()} must produce a set containing exactly
     * those names (trimmed).
     *
     * <p><b>Validates: Requirements 15.6</b></p>
     */
    @Property(tries = 100)
    void parsingCommaSeparatedProducesCorrectSet(
            @ForAll("uniqueToolNames") List<String> names) {

        McpConfig config = new McpConfig();
        config.setToolsDisabled(toCommaSeparated(names));

        Set<String> parsed = config.getDisabledToolNames();

        assertEquals(Set.copyOf(names), parsed,
                "Parsed set must match the original tool names");
    }

    /**
     * For any set of registered tools and a subset marked as disabled,
     * applying the disabled filter must remove exactly the disabled tools
     * from the registry.
     *
     * <p><b>Validates: Requirements 15.6, 15.7</b></p>
     */
    @Property(tries = 100)
    void disabledToolsRemovedFromRegistry(
            @ForAll("uniqueToolNames") List<String> allNames,
            @ForAll @net.jqwik.api.constraints.IntRange(min = 0, max = 100) int disablePercent) {

        // Register all tools
        McpToolRegistry registry = new McpToolRegistry();
        for (String name : allNames) {
            registry.register(toolDescriptor(name));
        }

        // Pick a subset to disable based on percentage
        int disableCount = Math.min(allNames.size(),
                (int) Math.ceil(allNames.size() * disablePercent / 100.0));
        List<String> toDisable = allNames.subList(0, disableCount);
        List<String> toKeep = allNames.subList(disableCount, allNames.size());

        // Configure and apply filter
        McpConfig config = new McpConfig();
        config.setToolsDisabled(toCommaSeparated(toDisable));
        registry.applyDisabledFilter(config.getDisabledToolNames());

        // Verify disabled tools are gone
        for (String disabled : toDisable) {
            assertTrue(registry.find(disabled).isEmpty(),
                    "Disabled tool '" + disabled + "' must not be in registry");
        }

        // Verify remaining tools are still present
        for (String kept : toKeep) {
            assertTrue(registry.find(kept).isPresent(),
                    "Non-disabled tool '" + kept + "' must remain in registry");
        }

        assertEquals(toKeep.size(), registry.size(),
                "Registry size must equal number of non-disabled tools");
    }

    /**
     * Empty or blank toolsDisabled must produce an empty set and leave
     * the registry unchanged.
     *
     * <p><b>Validates: Requirements 15.6</b></p>
     */
    @Property(tries = 100)
    void emptyDisabledStringProducesEmptySet(
            @ForAll("uniqueToolNames") List<String> allNames) {

        McpToolRegistry registry = new McpToolRegistry();
        for (String name : allNames) {
            registry.register(toolDescriptor(name));
        }

        McpConfig config = new McpConfig();
        // toolsDisabled defaults to ""
        Set<String> disabled = config.getDisabledToolNames();
        assertTrue(disabled.isEmpty(), "Empty toolsDisabled must produce empty set");

        registry.applyDisabledFilter(disabled);
        assertEquals(allNames.size(), registry.size(),
                "Registry must be unchanged when no tools are disabled");
    }
}
