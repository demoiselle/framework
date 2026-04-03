/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.registry;

import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class McpResourceRegistryTest {

    private McpResourceRegistry registry;
    private Method sampleMethod;

    @BeforeEach
    void setUp() throws NoSuchMethodException {
        registry = new McpResourceRegistry();
        sampleMethod = String.class.getMethod("toString");
    }

    private ResourceDescriptor descriptor(String uri) {
        return new ResourceDescriptor(uri, "name-" + uri, "desc-" + uri, "text/plain", this, sampleMethod);
    }

    @Test
    void registerAndFindByUri() {
        ResourceDescriptor rd = descriptor("file:///data.txt");
        registry.register(rd);

        Optional<ResourceDescriptor> found = registry.findByUri("file:///data.txt");
        assertTrue(found.isPresent());
        assertEquals(rd, found.get());
    }

    @Test
    void findByUriReturnsEmptyForUnknownUri() {
        assertTrue(registry.findByUri("file:///nonexistent").isEmpty());
    }

    @Test
    void registerDuplicateUriThrowsIllegalState() {
        registry.register(descriptor("file:///dup"));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> registry.register(descriptor("file:///dup")));
        assertTrue(ex.getMessage().contains("file:///dup"));
    }

    @Test
    void listAllReturnsAllRegistered() {
        registry.register(descriptor("uri:a"));
        registry.register(descriptor("uri:b"));
        registry.register(descriptor("uri:c"));

        List<ResourceDescriptor> all = registry.listAll();
        assertEquals(3, all.size());

        Set<String> uris = Set.of(all.get(0).uri(), all.get(1).uri(), all.get(2).uri());
        assertTrue(uris.containsAll(Set.of("uri:a", "uri:b", "uri:c")));
    }

    @Test
    void listAllReturnsUnmodifiableList() {
        registry.register(descriptor("uri:x"));

        List<ResourceDescriptor> all = registry.listAll();
        assertThrows(UnsupportedOperationException.class, () -> all.add(descriptor("uri:y")));
    }

    @Test
    void sizeReflectsRegisteredCount() {
        assertEquals(0, registry.size());
        registry.register(descriptor("uri:one"));
        assertEquals(1, registry.size());
        registry.register(descriptor("uri:two"));
        assertEquals(2, registry.size());
    }

    @Test
    void registerNullDescriptorThrowsNPE() {
        assertThrows(NullPointerException.class, () -> registry.register(null));
    }

    @Test
    void registerDescriptorWithNullUriThrowsNPE() {
        ResourceDescriptor rd = new ResourceDescriptor(null, "name", "desc", "text/plain", this, sampleMethod);
        assertThrows(NullPointerException.class, () -> registry.register(rd));
    }
}
