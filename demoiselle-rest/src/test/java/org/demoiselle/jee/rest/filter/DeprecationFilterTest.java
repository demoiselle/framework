/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.rest.annotation.ApiLifecycle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for {@link DeprecationFilter}.
 *
 * <p>Validates Requirement (6): Deprecation, Sunset and Link headers.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeprecationFilterTest {

    @ApiLifecycle(since = "2025-01-01", sunset = "2025-12-31",
            link = "https://docs.example/deprecation", successor = "https://api.example/v2/users")
    static class FullyAnnotated {
        public void op() { }
    }

    @ApiLifecycle
    static class MinimalAnnotated {
        public void op() { }
    }

    static class NotAnnotated {
        public void op() { }
    }

    @Mock ContainerRequestContext request;
    @Mock ContainerResponseContext response;
    @Mock ResourceInfo resourceInfo;

    MultivaluedMap<String, Object> headers;
    DeprecationFilter filter;

    @BeforeEach
    void setUp() throws Exception {
        headers = new MultivaluedHashMap<>();
        when(response.getHeaders()).thenReturn(headers);
        filter = new DeprecationFilter();
        inject(filter, "resourceInfo", resourceInfo);
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void bind(Class<?> resourceClass) throws Exception {
        Method m = resourceClass.getDeclaredMethod("op");
        when(resourceInfo.getResourceMethod()).thenReturn(m);
        when(resourceInfo.getResourceClass()).thenAnswer(i -> resourceClass);
    }

    @Test
    @DisplayName("Fully annotated resource emits Deprecation, Sunset and Link headers")
    void emitsAllHeaders() throws Exception {
        bind(FullyAnnotated.class);

        filter.filter(request, response);

        // Deprecation is an IMF-fixdate derived from the since date.
        assertEquals("Wed, 01 Jan 2025 00:00:00 GMT", headers.getFirst(DeprecationFilter.DEPRECATION));
        assertEquals("Wed, 31 Dec 2025 00:00:00 GMT", headers.getFirst(DeprecationFilter.SUNSET));

        List<Object> links = headers.get(DeprecationFilter.LINK);
        assertTrue(links.stream().anyMatch(l -> l.toString()
                .equals("<https://docs.example/deprecation>; rel=\"deprecation\"")));
        assertTrue(links.stream().anyMatch(l -> l.toString()
                .equals("<https://api.example/v2/users>; rel=\"successor-version\"")));
    }

    @Test
    @DisplayName("Minimal annotation emits Deprecation: true and no Sunset/Link")
    void emitsMinimal() throws Exception {
        bind(MinimalAnnotated.class);

        filter.filter(request, response);

        assertEquals("true", headers.getFirst(DeprecationFilter.DEPRECATION));
        assertFalse(headers.containsKey(DeprecationFilter.SUNSET));
        assertFalse(headers.containsKey(DeprecationFilter.LINK));
    }

    @Test
    @DisplayName("Unannotated resource emits no lifecycle headers")
    void emitsNothingWhenUnannotated() throws Exception {
        bind(NotAnnotated.class);

        filter.filter(request, response);

        assertFalse(headers.containsKey(DeprecationFilter.DEPRECATION));
        assertFalse(headers.containsKey(DeprecationFilter.SUNSET));
    }

    @Test
    @DisplayName("Existing Deprecation header set by application is not overridden")
    void doesNotOverrideExisting() throws Exception {
        bind(FullyAnnotated.class);
        headers.putSingle(DeprecationFilter.DEPRECATION, "app-managed");

        filter.filter(request, response);

        assertEquals("app-managed", headers.getFirst(DeprecationFilter.DEPRECATION));
    }
}
