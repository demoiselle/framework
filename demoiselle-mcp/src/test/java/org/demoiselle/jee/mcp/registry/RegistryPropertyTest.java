/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.registry;

import net.jqwik.api.*;

import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;
import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;
import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for the MCP registry classes.
 *
 * <p>Covers Property 4 (duplicate rejection) and Property 5 (listing consistency)
 * for all three registries: McpToolRegistry, McpResourceRegistry, McpPromptRegistry.</p>
 */
class RegistryPropertyTest {

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
    Arbitrary<String> registryNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(50);
    }

    @Provide
    Arbitrary<String> resourceUris() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30)
                .map(s -> "mcp://" + s);
    }

    @Provide
    Arbitrary<List<String>> uniqueNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30)
                .list()
                .ofMinSize(1)
                .ofMaxSize(20)
                .uniqueElements();
    }

    @Provide
    Arbitrary<List<String>> uniqueUris() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30)
                .map(s -> "mcp://" + s)
                .list()
                .ofMinSize(1)
                .ofMaxSize(20)
                .uniqueElements();
    }

    // -----------------------------------------------------------------------
    // Helper factory methods
    // -----------------------------------------------------------------------

    private ToolDescriptor toolDescriptor(String name) {
        return new ToolDescriptor(name, "desc-" + name, Map.of(), this, DUMMY_METHOD);
    }

    private ResourceDescriptor resourceDescriptor(String uri) {
        return new ResourceDescriptor(uri, "name-" + uri, "desc", "text/plain", this, DUMMY_METHOD);
    }

    private PromptDescriptor promptDescriptor(String name) {
        return new PromptDescriptor(name, "desc-" + name, List.of(), this, DUMMY_METHOD);
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 4: Rejeição de nomes/URIs duplicados
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 4: Rejeição de nomes/URIs duplicados nos registros
    /**
     * For any random tool name, registering two ToolDescriptors with the same name
     * must throw IllegalStateException on the second register() call.
     *
     * <p><b>Validates: Requirements 2.3</b></p>
     */
    @Property(tries = 100)
    void duplicateToolNameThrowsIllegalState(@ForAll("registryNames") String name) {
        McpToolRegistry registry = new McpToolRegistry();
        registry.register(toolDescriptor(name));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registry.register(toolDescriptor(name)),
                "Second register() with same tool name '" + name + "' must throw IllegalStateException");
        assertTrue(ex.getMessage().contains(name),
                "Exception message must mention the duplicate name '" + name + "'");
    }

    // Feature: demoiselle-mcp, Property 4: Rejeição de nomes/URIs duplicados nos registros
    /**
     * For any random resource URI, registering two ResourceDescriptors with the same URI
     * must throw IllegalStateException on the second register() call.
     *
     * <p><b>Validates: Requirements 8.6</b></p>
     */
    @Property(tries = 100)
    void duplicateResourceUriThrowsIllegalState(@ForAll("resourceUris") String uri) {
        McpResourceRegistry registry = new McpResourceRegistry();
        registry.register(resourceDescriptor(uri));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registry.register(resourceDescriptor(uri)),
                "Second register() with same resource URI '" + uri + "' must throw IllegalStateException");
        assertTrue(ex.getMessage().contains(uri),
                "Exception message must mention the duplicate URI '" + uri + "'");
    }

    // Feature: demoiselle-mcp, Property 4: Rejeição de nomes/URIs duplicados nos registros
    /**
     * For any random prompt name, registering two PromptDescriptors with the same name
     * must throw IllegalStateException on the second register() call.
     *
     * <p><b>Validates: Requirements 9.6</b></p>
     */
    @Property(tries = 100)
    void duplicatePromptNameThrowsIllegalState(@ForAll("registryNames") String name) {
        McpPromptRegistry registry = new McpPromptRegistry();
        registry.register(promptDescriptor(name));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registry.register(promptDescriptor(name)),
                "Second register() with same prompt name '" + name + "' must throw IllegalStateException");
        assertTrue(ex.getMessage().contains(name),
                "Exception message must mention the duplicate name '" + name + "'");
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 5: Consistência entre registros e respostas de listagem
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 5: Consistência entre registros e respostas de listagem
    /**
     * For any set of unique tool names, after registering all of them,
     * listAll() must return exactly those names with the same count.
     *
     * <p><b>Validates: Requirements 2.4, 2.5</b></p>
     */
    @Property(tries = 100)
    void toolRegistryListAllMatchesRegistered(@ForAll("uniqueNames") List<String> names) {
        McpToolRegistry registry = new McpToolRegistry();
        for (String name : names) {
            registry.register(toolDescriptor(name));
        }

        List<ToolDescriptor> listed = registry.listAll();
        assertEquals(names.size(), listed.size(),
                "listAll() size must equal number of registered tools");

        Set<String> listedNames = listed.stream()
                .map(ToolDescriptor::name)
                .collect(Collectors.toSet());
        assertEquals(Set.copyOf(names), listedNames,
                "listAll() names must match exactly the registered names");
    }

    // Feature: demoiselle-mcp, Property 5: Consistência entre registros e respostas de listagem
    /**
     * For any set of unique resource URIs, after registering all of them,
     * listAll() must return exactly those URIs with the same count.
     *
     * <p><b>Validates: Requirements 8.3</b></p>
     */
    @Property(tries = 100)
    void resourceRegistryListAllMatchesRegistered(@ForAll("uniqueUris") List<String> uris) {
        McpResourceRegistry registry = new McpResourceRegistry();
        for (String uri : uris) {
            registry.register(resourceDescriptor(uri));
        }

        List<ResourceDescriptor> listed = registry.listAll();
        assertEquals(uris.size(), listed.size(),
                "listAll() size must equal number of registered resources");

        Set<String> listedUris = listed.stream()
                .map(ResourceDescriptor::uri)
                .collect(Collectors.toSet());
        assertEquals(Set.copyOf(uris), listedUris,
                "listAll() URIs must match exactly the registered URIs");
    }

    // Feature: demoiselle-mcp, Property 5: Consistência entre registros e respostas de listagem
    /**
     * For any set of unique prompt names, after registering all of them,
     * listAll() must return exactly those names with the same count.
     *
     * <p><b>Validates: Requirements 9.3</b></p>
     */
    @Property(tries = 100)
    void promptRegistryListAllMatchesRegistered(@ForAll("uniqueNames") List<String> names) {
        McpPromptRegistry registry = new McpPromptRegistry();
        for (String name : names) {
            registry.register(promptDescriptor(name));
        }

        List<PromptDescriptor> listed = registry.listAll();
        assertEquals(names.size(), listed.size(),
                "listAll() size must equal number of registered prompts");

        Set<String> listedNames = listed.stream()
                .map(PromptDescriptor::name)
                .collect(Collectors.toSet());
        assertEquals(Set.copyOf(names), listedNames,
                "listAll() names must match exactly the registered names");
    }
}
