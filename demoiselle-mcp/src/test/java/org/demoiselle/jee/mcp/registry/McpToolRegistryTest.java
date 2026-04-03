/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.registry;

import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class McpToolRegistryTest {

    private McpToolRegistry registry;
    private Method sampleMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        registry = new McpToolRegistry();
        sampleMethod = String.class.getMethod("toString");
    }

    private ToolDescriptor descriptor(String name) {
        return new ToolDescriptor(name, "desc-" + name, Map.of(), this, sampleMethod);
    }

    @Test
    void registerAndFindTool() {
        ToolDescriptor td = descriptor("myTool");
        registry.register(td);

        Optional<ToolDescriptor> found = registry.find("myTool");
        assertTrue(found.isPresent());
        assertEquals(td, found.get());
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

        List<ToolDescriptor> all = registry.listAll();
        assertEquals(3, all.size());

        Set<String> names = Set.of(all.get(0).name(), all.get(1).name(), all.get(2).name());
        assertTrue(names.containsAll(Set.of("a", "b", "c")));
    }

    @Test
    void listAllReturnsUnmodifiableList() {
        registry.register(descriptor("x"));

        List<ToolDescriptor> all = registry.listAll();
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
    void applyDisabledFilterRemovesMatchingTools() {
        registry.register(descriptor("keep"));
        registry.register(descriptor("remove1"));
        registry.register(descriptor("remove2"));

        registry.applyDisabledFilter(Set.of("remove1", "remove2"));

        assertEquals(1, registry.size());
        assertTrue(registry.find("keep").isPresent());
        assertTrue(registry.find("remove1").isEmpty());
        assertTrue(registry.find("remove2").isEmpty());
    }

    @Test
    void applyDisabledFilterWithNullSetIsNoOp() {
        registry.register(descriptor("safe"));
        registry.applyDisabledFilter(null);
        assertEquals(1, registry.size());
    }

    @Test
    void applyDisabledFilterWithEmptySetIsNoOp() {
        registry.register(descriptor("safe"));
        registry.applyDisabledFilter(Set.of());
        assertEquals(1, registry.size());
    }

    @Test
    void applyDisabledFilterIgnoresNonExistentNames() {
        registry.register(descriptor("exists"));
        registry.applyDisabledFilter(Set.of("ghost"));
        assertEquals(1, registry.size());
        assertTrue(registry.find("exists").isPresent());
    }

    @Test
    void registerNullDescriptorThrowsNPE() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void registerDescriptorWithNullNameThrowsNPE() {
        ToolDescriptor td = new ToolDescriptor(null, "desc", Map.of(), this, sampleMethod);
        assertThrows(NullPointerException.class, () -> registry.register(td));
    }
}
