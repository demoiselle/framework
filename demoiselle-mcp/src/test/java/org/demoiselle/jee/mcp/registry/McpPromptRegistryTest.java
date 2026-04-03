/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.registry;

import org.demoiselle.jee.mcp.descriptor.PromptArgument;
import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class McpPromptRegistryTest {

    private McpPromptRegistry registry;
    private Method sampleMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        registry = new McpPromptRegistry();
        sampleMethod = String.class.getMethod("toString");
    }

    private PromptDescriptor descriptor(String name) {
        return new PromptDescriptor(name, "desc-" + name, List.of(), this, sampleMethod);
    }

    @Test
    void registerAndFindPrompt() {
        PromptDescriptor pd = descriptor("myPrompt");
        registry.register(pd);

        Optional<PromptDescriptor> found = registry.find("myPrompt");
        assertTrue(found.isPresent());
        assertEquals(pd, found.get());
    }

    @Test
    void findReturnsEmptyForUnknownName() {
        assertTrue(registry.find("nonexistent").isEmpty());
    }

    @Test
    void registerDuplicateNameThrowsIllegalState() {
        registry.register(descriptor("dup"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registry.register(descriptor("dup")));
        assertTrue(ex.getMessage().contains("dup"));
    }

    @Test
    void listAllReturnsAllRegistered() {
        registry.register(descriptor("a"));
        registry.register(descriptor("b"));
        registry.register(descriptor("c"));

        List<PromptDescriptor> all = registry.listAll();
        assertEquals(3, all.size());

        Set<String> names = Set.of(all.get(0).name(), all.get(1).name(), all.get(2).name());
        assertTrue(names.containsAll(Set.of("a", "b", "c")));
    }

    @Test
    void listAllReturnsUnmodifiableList() {
        registry.register(descriptor("x"));

        List<PromptDescriptor> all = registry.listAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(descriptor("y")));
    }

    @Test
    void sizeReflectsRegisteredCount() {
        assertEquals(0, registry.size());
        registry.register(descriptor("one"));
        assertEquals(1, registry.size());
        registry.register(descriptor("two"));
        assertEquals(2, registry.size());
    }

    @Test
    void registerNullDescriptorThrowsNPE() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void registerDescriptorWithNullNameThrowsNPE() {
        PromptDescriptor pd = new PromptDescriptor(null, "desc", List.of(), this, sampleMethod);
        assertThrows(NullPointerException.class, () -> registry.register(pd));
    }

    @Test
    void promptDescriptorPreservesArguments() {
        List<PromptArgument> args = List.of(
                new PromptArgument("topic", "The topic to discuss", true),
                new PromptArgument("style", "Writing style", false)
        );
        PromptDescriptor pd = new PromptDescriptor("withArgs", "desc", args, this, sampleMethod);
        registry.register(pd);

        Optional<PromptDescriptor> found = registry.find("withArgs");
        assertTrue(found.isPresent());
        assertEquals(2, found.get().arguments().size());
        assertEquals("topic", found.get().arguments().get(0).name());
        assertTrue(found.get().arguments().get(0).required());
        assertEquals("style", found.get().arguments().get(1).name());
        assertFalse(found.get().arguments().get(1).required());
    }
}
