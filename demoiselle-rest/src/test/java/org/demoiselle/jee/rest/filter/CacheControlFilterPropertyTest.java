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

import net.jqwik.api.*;

import org.demoiselle.jee.rest.annotation.CacheControl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Property-based tests for {@link CacheControlFilter}.
 */
class CacheControlFilterPropertyTest {

    // ---------------------------------------------------------------
    // Test resource with @CacheControl annotation
    // ---------------------------------------------------------------

    @CacheControl(maxAge = 300)
    static class AnnotatedResource {
        @CacheControl(maxAge = 600)
        public void annotatedMethod() {}
    }

    // ---------------------------------------------------------------
    // Providers
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<String> nonGetMethods() {
        return Arbitraries.of("POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD");
    }

    // ---------------------------------------------------------------
    // Property 11: Non-GET requests are ignored
    // ---------------------------------------------------------------

    /**
     * // Feature: rest-enhancements, Property 11: CacheControlFilter ignora requisições não-GET
     *
     * For any HTTP method other than "GET", the CacheControlFilter must not
     * add the Cache-Control header to the response, even if @CacheControl
     * is present.
     *
     * **Validates: Requirements 7.4**
     */
    @Property(tries = 100)
    void nonGetRequestsDoNotReceiveCacheControlHeader(
            @ForAll("nonGetMethods") String httpMethod) throws Exception {

        // Set up mocks
        ContainerRequestContext req = mock(ContainerRequestContext.class);
        ContainerResponseContext res = mock(ContainerResponseContext.class);
        ResourceInfo resourceInfo = mock(ResourceInfo.class);

        when(req.getMethod()).thenReturn(httpMethod);

        // Configure ResourceInfo to return an annotated method and class
        Method method = AnnotatedResource.class.getMethod("annotatedMethod");
        when(resourceInfo.getResourceMethod()).thenReturn(method);
        when(resourceInfo.getResourceClass()).thenReturn((Class) AnnotatedResource.class);

        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(res.getHeaders()).thenReturn(headers);

        // Create filter and inject ResourceInfo via reflection
        CacheControlFilter filter = new CacheControlFilter();
        java.lang.reflect.Field infoField = CacheControlFilter.class.getDeclaredField("info");
        infoField.setAccessible(true);
        infoField.set(filter, resourceInfo);

        // Execute filter
        filter.filter(req, res);

        // Verify: Cache-Control header must NOT be present
        assertFalse(headers.containsKey("Cache-Control"),
                "Cache-Control header must not be set for " + httpMethod + " requests");
        verify(res, never()).getHeaders();
    }
}
