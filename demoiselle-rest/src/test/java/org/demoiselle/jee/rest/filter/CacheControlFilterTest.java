/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.lang.reflect.Method;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.rest.annotation.CacheControl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link CacheControlFilter}.
 */
class CacheControlFilterTest {

    // ---------------------------------------------------------------
    // Test resource classes with @CacheControl annotations
    // ---------------------------------------------------------------

    static class LegacyValueResource {
        @CacheControl("no-store")
        public void legacyMethod() {}
    }

    static class TypedAttributesResource {
        @CacheControl(maxAge = 3600, noCache = true)
        public void typedMethod() {}
    }

    @CacheControl(maxAge = 60)
    static class ClassLevelResource {
        @CacheControl(maxAge = 300)
        public void methodWithOverride() {}

        public void methodWithoutAnnotation() {}
    }

    static class NoAnnotationResource {
        public void plainMethod() {}
    }

    // ---------------------------------------------------------------
    // Common test fixtures
    // ---------------------------------------------------------------

    private CacheControlFilter filter;
    private ContainerRequestContext req;
    private ContainerResponseContext res;
    private ResourceInfo resourceInfo;
    private MultivaluedMap<String, Object> headers;

    @BeforeEach
    void setUp() throws Exception {
        filter = new CacheControlFilter();
        req = mock(ContainerRequestContext.class);
        res = mock(ContainerResponseContext.class);
        resourceInfo = mock(ResourceInfo.class);

        headers = new MultivaluedHashMap<>();
        when(res.getHeaders()).thenReturn(headers);

        // Inject ResourceInfo via reflection
        java.lang.reflect.Field infoField = CacheControlFilter.class.getDeclaredField("info");
        infoField.setAccessible(true);
        infoField.set(filter, resourceInfo);
    }

    // ---------------------------------------------------------------
    // Test: value() legado
    // ---------------------------------------------------------------

    /** Method annotated with @CacheControl("no-store") → header is "no-store". */
    @Test
    void legacyValueProducesLiteralHeader() throws Exception {
        when(req.getMethod()).thenReturn("GET");

        Method method = LegacyValueResource.class.getMethod("legacyMethod");
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        when(resourceInfo.getResourceClass()).thenReturn((Class) LegacyValueResource.class);

        filter.filter(req, res);

        assertEquals("no-store", headers.getFirst("Cache-Control"));
    }

    // ---------------------------------------------------------------
    // Test: atributos tipados
    // ---------------------------------------------------------------

    /** Method annotated with typed attributes → header built from CacheControlBuilder. */
    @Test
    void typedAttributesProduceBuiltHeader() throws Exception {
        when(req.getMethod()).thenReturn("GET");

        Method method = TypedAttributesResource.class.getMethod("typedMethod");
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        when(resourceInfo.getResourceClass()).thenReturn((Class) TypedAttributesResource.class);

        filter.filter(req, res);

        String expected = "max-age=3600, no-cache, public";
        assertEquals(expected, headers.getFirst("Cache-Control"));
    }

    // ---------------------------------------------------------------
    // Test: prioridade método sobre tipo
    // ---------------------------------------------------------------

    /** Class has @CacheControl(maxAge=60), method has @CacheControl(maxAge=300) → method wins. */
    @Test
    void methodAnnotationTakesPriorityOverClass() throws Exception {
        when(req.getMethod()).thenReturn("GET");

        Method method = ClassLevelResource.class.getMethod("methodWithOverride");
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        when(resourceInfo.getResourceClass()).thenReturn((Class) ClassLevelResource.class);

        filter.filter(req, res);

        String header = (String) headers.getFirst("Cache-Control");
        assertTrue(header.contains("max-age=300"),
                "Method-level maxAge=300 should take priority, got: " + header);
        assertFalse(header.contains("max-age=60"),
                "Class-level maxAge=60 should NOT be present, got: " + header);
    }

    /** Method without annotation falls back to class-level annotation. */
    @Test
    void classAnnotationUsedWhenMethodHasNone() throws Exception {
        when(req.getMethod()).thenReturn("GET");

        Method method = ClassLevelResource.class.getMethod("methodWithoutAnnotation");
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        when(resourceInfo.getResourceClass()).thenReturn((Class) ClassLevelResource.class);

        filter.filter(req, res);

        String header = (String) headers.getFirst("Cache-Control");
        assertTrue(header.contains("max-age=60"),
                "Class-level maxAge=60 should be used, got: " + header);
    }

    /** Non-GET request does not add Cache-Control header. */
    @Test
    void nonGetRequestIgnored() throws Exception {
        when(req.getMethod()).thenReturn("POST");

        Method method = LegacyValueResource.class.getMethod("legacyMethod");
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        when(resourceInfo.getResourceClass()).thenReturn((Class) LegacyValueResource.class);

        filter.filter(req, res);

        assertFalse(headers.containsKey("Cache-Control"),
                "Cache-Control header should not be set for POST requests");
    }

    /** No annotation on method or class → no Cache-Control header. */
    @Test
    void noAnnotationProducesNoHeader() throws Exception {
        when(req.getMethod()).thenReturn("GET");

        Method method = NoAnnotationResource.class.getMethod("plainMethod");
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        when(resourceInfo.getResourceClass()).thenReturn((Class) NoAnnotationResource.class);

        filter.filter(req, res);

        assertFalse(headers.containsKey("Cache-Control"),
                "Cache-Control header should not be set when no annotation is present");
    }
}
