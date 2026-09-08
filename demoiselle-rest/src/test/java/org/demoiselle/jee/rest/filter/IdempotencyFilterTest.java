/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.core.UriInfo;

import org.demoiselle.jee.core.api.idempotency.IdempotencyRecord;
import org.demoiselle.jee.core.idempotency.LocalIdempotencyStore;
import org.demoiselle.jee.rest.annotation.Idempotent;
import org.demoiselle.jee.rest.exception.treatment.TestRuntimeDelegate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

/**
 * Unit tests for {@link IdempotencyFilter}.
 *
 * <p>Validates Requirement (3): Idempotency-Key handling, replay of completed
 * responses, 409 on in-progress conflict, and opt-in behaviour.</p>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IdempotencyFilterTest {

    private record Created(String id, int quantity) { }

    @Idempotent
    static class SampleResource {
        @Idempotent
        public void create() { }
    }

    @Mock ContainerRequestContext request;
    @Mock ContainerResponseContext response;
    @Mock ResourceInfo resourceInfo;
    @Mock SecurityContext securityContext;
    @Mock UriInfo uriInfo;

    LocalIdempotencyStore store;
    IdempotencyFilter filter;

    @BeforeAll
    static void installDelegate() {
        TestRuntimeDelegate.install();
    }

    @BeforeEach
    void setUp() throws Exception {
        store = new LocalIdempotencyStore();
        filter = new IdempotencyFilter();
        inject(filter, "store", store);
        inject(filter, "resourceInfo", resourceInfo);
        inject(filter, "securityContext", securityContext);

        when(resourceInfo.getResourceMethod()).thenReturn(sampleMethod());
        when(resourceInfo.getResourceClass()).thenAnswer(i -> SampleResource.class);
        when(request.getMethod()).thenReturn("POST");
        when(request.getUriInfo()).thenReturn(uriInfo);
        when(uriInfo.getPath()).thenReturn("orders");
        when(securityContext.getUserPrincipal()).thenReturn(() -> "alice");
    }

    private static Method sampleMethod() throws NoSuchMethodException {
        return SampleResource.class.getDeclaredMethod("create");
    }

    private static void inject(Object target, String field, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(field);
        f.setAccessible(true);
        f.set(target, value);
    }

    private void withKeyAndBody(String key, String body) {
        when(request.getHeaderString(IdempotencyFilter.HEADER_KEY)).thenReturn(key);
        when(request.getEntityStream()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
    }

    @Test
    @DisplayName("No Idempotency-Key → no store interaction (opt-in)")
    void noKeyIsNoOp() throws Exception {
        when(request.getHeaderString(IdempotencyFilter.HEADER_KEY)).thenReturn(null);

        filter.filter(request);

        verify(request, never()).setProperty(anyString(), any());
        verify(request, never()).abortWith(any());
        assertEquals(0, store.size());
    }

    @Test
    @DisplayName("Fresh key → slot acquired, request proceeds")
    void freshKeyProceeds() throws Exception {
        withKeyAndBody("k-1", "{\"a\":1}");

        filter.filter(request);

        verify(request, never()).abortWith(any());
        verify(request).setProperty(eq(IdempotencyFilter.PROP_FINGERPRINT), any());
        assertEquals(1, store.size());
    }

    @Test
    @DisplayName("In-progress duplicate → 409 Conflict")
    void inProgressConflict() throws Exception {
        withKeyAndBody("k-2", "{\"a\":1}");

        // First request acquires the slot.
        filter.filter(request);
        // Second identical request should conflict.
        ContainerRequestContext request2 = freshRequest("k-2", "{\"a\":1}");

        filter.filter(request2);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(request2).abortWith(captor.capture());
        assertEquals(Response.Status.CONFLICT.getStatusCode(), captor.getValue().getStatus());
    }

    @Test
    @DisplayName("Completed key → replay stored response")
    void completedReplays() throws Exception {
        withKeyAndBody("k-3", "{\"a\":1}");
        filter.filter(request);

        // Capture the fingerprint the filter stored and mark it complete.
        ArgumentCaptor<String> fp = ArgumentCaptor.forClass(String.class);
        verify(request).setProperty(eq(IdempotencyFilter.PROP_FINGERPRINT), fp.capture());
        store.complete(fp.getValue(), 201, "created".getBytes(), Map.of());

        ContainerRequestContext request2 = freshRequest("k-3", "{\"a\":1}");
        filter.filter(request2);

        ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
        verify(request2).abortWith(captor.capture());
        assertEquals(201, captor.getValue().getStatus());
        assertArrayEquals("created".getBytes(StandardCharsets.UTF_8),
                (byte[]) captor.getValue().getEntity());
    }

    @Test
    @DisplayName("Response filter stores 2xx and aborts slot on 5xx")
    void responsePassCompletesOrAborts() throws Exception {
        // 2xx completes
        when(request.getProperty(IdempotencyFilter.PROP_FINGERPRINT)).thenReturn("fp-2xx");
        MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
        when(response.getHeaders()).thenReturn(headers);
        when(response.getStatus()).thenReturn(200);
        when(response.getEntity()).thenReturn("ok");
        store.begin("fp-2xx", 60);

        filter.filter(request, response);
        assertEquals(IdempotencyRecord.Status.COMPLETED, store.find("fp-2xx").orElseThrow().status());

        // POJO responses are stored as JSON, never as Object#toString().
        when(request.getProperty(IdempotencyFilter.PROP_FINGERPRINT)).thenReturn("fp-pojo");
        when(response.getEntity()).thenReturn(new Created("42", 3));
        store.begin("fp-pojo", 60);
        filter.filter(request, response);
        String json = new String(store.find("fp-pojo").orElseThrow().body(), StandardCharsets.UTF_8);
        assertEquals("{\"id\":\"42\",\"quantity\":3}", json);

        // 5xx aborts
        when(request.getProperty(IdempotencyFilter.PROP_FINGERPRINT)).thenReturn("fp-5xx");
        when(response.getStatus()).thenReturn(500);
        store.begin("fp-5xx", 60);

        filter.filter(request, response);
        assertEquals(Optional.empty(), store.find("fp-5xx"));
    }

    private ContainerRequestContext freshRequest(String key, String body) {
        ContainerRequestContext req = org.mockito.Mockito.mock(ContainerRequestContext.class);
        when(req.getHeaderString(IdempotencyFilter.HEADER_KEY)).thenReturn(key);
        when(req.getEntityStream()).thenReturn(new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8)));
        when(req.getMethod()).thenReturn("POST");
        when(req.getUriInfo()).thenReturn(uriInfo);
        return req;
    }

    // Keep import used
    @SuppressWarnings("unused")
    private static final List<String> UNUSED = List.of();
}
