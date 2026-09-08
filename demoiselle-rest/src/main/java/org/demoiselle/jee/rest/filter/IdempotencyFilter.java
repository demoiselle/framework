/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import jakarta.ws.rs.ext.Provider;

import org.demoiselle.jee.core.api.idempotency.IdempotencyRecord;
import org.demoiselle.jee.core.api.idempotency.IdempotencyStore;
import org.demoiselle.jee.core.idempotency.IdempotencyFingerprint;
import org.demoiselle.jee.rest.annotation.Idempotent;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Request/response filter that enforces idempotency for resources annotated with
 * {@link Idempotent}.
 *
 * <h2>Behaviour</h2>
 * <ol>
 *   <li>On the way in, if an {@code Idempotency-Key} header is present the filter
 *       buffers the request body, computes a fingerprint of
 *       {@code principal + method + path + payloadHash} and atomically
 *       {@link IdempotencyStore#begin(String, long) claims} the slot.</li>
 *   <li>If the slot was already <strong>completed</strong>, the stored response
 *       is replayed and the request is short-circuited (with an
 *       {@code Idempotency-Replayed: true} marker header).</li>
 *   <li>If the slot is <strong>in progress</strong>, a {@code 409 Conflict} is
 *       returned.</li>
 *   <li>Otherwise processing continues; on the way out the response is captured
 *       into the store (for {@code 2xx}) or the in-progress slot is aborted so
 *       the client may retry (for errors).</li>
 * </ol>
 *
 * <p>
 * If no {@code Idempotency-Key} header is supplied the filter is a no-op, so the
 * feature is strictly opt-in per request.
 * </p>
 *
 * @author SERPRO
 */
@Provider
@Idempotent
@Priority(Priorities.IDEMPOTENCY)
public class IdempotencyFilter implements ContainerRequestFilter, ContainerResponseFilter {

    /** Header carrying the client-supplied idempotency key. */
    public static final String HEADER_KEY = "Idempotency-Key";
    /** Marker header set on replayed responses. */
    public static final String HEADER_REPLAYED = "Idempotency-Replayed";
    /** Request property used to carry the fingerprint between the two filter passes. */
    static final String PROP_FINGERPRINT = "org.demoiselle.idempotency.fingerprint";

    private static final ObjectMapper JSON = new ObjectMapper();

    @Inject
    private IdempotencyStore store;

    @Context
    private ResourceInfo resourceInfo;

    @Context
    private SecurityContext securityContext;

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String key = requestContext.getHeaderString(HEADER_KEY);
        if (key == null || key.isBlank() || store == null) {
            return; // opt-in: no key means no idempotency handling
        }

        Idempotent annotation = resolveAnnotation();
        boolean includePayload = annotation == null || annotation.includePayload();
        long ttl = annotation == null ? 86_400L : annotation.ttlSeconds();

        byte[] payload = includePayload ? bufferEntity(requestContext) : null;

        String principal = securityContext != null && securityContext.getUserPrincipal() != null
                ? securityContext.getUserPrincipal().getName()
                : null;
        String method = requestContext.getMethod();
        String path = requestContext.getUriInfo() != null
                ? requestContext.getUriInfo().getPath()
                : "";

        String fingerprint = IdempotencyFingerprint.compute(key, principal, method, path, payload, includePayload);
        requestContext.setProperty(PROP_FINGERPRINT, fingerprint);

        Optional<IdempotencyRecord> existing = store.begin(fingerprint, ttl);
        if (existing.isEmpty()) {
            return; // won the race: process normally, capture on the way out
        }

        IdempotencyRecord record = existing.get();
        if (record.status() == IdempotencyRecord.Status.COMPLETED) {
            requestContext.abortWith(replay(record));
        } else {
            // Identical request still in progress → conflict.
            requestContext.abortWith(Response.status(Response.Status.CONFLICT)
                    .header("Retry-After", "1")
                    .entity("A request with this Idempotency-Key is already in progress.")
                    .build());
        }
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
        Object fp = requestContext.getProperty(PROP_FINGERPRINT);
        if (!(fp instanceof String fingerprint) || store == null) {
            return;
        }
        // A replayed response already carries the marker header — don't re-store.
        if (responseContext.getHeaders().containsKey(HEADER_REPLAYED)) {
            return;
        }

        int status = responseContext.getStatus();
        if (status >= 200 && status < 300) {
            try {
                store.complete(fingerprint, status, serialiseEntity(responseContext), copyHeaders(responseContext));
            } catch (IllegalStateException serializationFailure) {
                // Never cache an inaccurate representation. Releasing the slot
                // is safer than replaying a POJO's toString() as an HTTP body.
                store.abort(fingerprint);
            }
        } else {
            // Failure: release the slot so the client can retry with the same key.
            store.abort(fingerprint);
        }
    }

    private Idempotent resolveAnnotation() {
        if (resourceInfo == null) {
            return null;
        }
        Method method = resourceInfo.getResourceMethod();
        if (method != null && method.isAnnotationPresent(Idempotent.class)) {
            return method.getAnnotation(Idempotent.class);
        }
        Class<?> clazz = resourceInfo.getResourceClass();
        if (clazz != null && clazz.isAnnotationPresent(Idempotent.class)) {
            return clazz.getAnnotation(Idempotent.class);
        }
        return null;
    }

    private static byte[] bufferEntity(ContainerRequestContext ctx) throws IOException {
        InputStream in = ctx.getEntityStream();
        if (in == null) {
            return new byte[0];
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] chunk = new byte[4096];
        int read;
        while ((read = in.read(chunk)) != -1) {
            buffer.write(chunk, 0, read);
        }
        byte[] bytes = buffer.toByteArray();
        // Reset the stream so downstream message body readers still see the body.
        ctx.setEntityStream(new ByteArrayInputStream(bytes));
        return bytes;
    }

    private static byte[] serialiseEntity(ContainerResponseContext responseContext) {
        Object entity = responseContext.getEntity();
        if (entity == null) {
            return null;
        }
        if (entity instanceof byte[] bytes) {
            return bytes.clone();
        }
        if (entity instanceof CharSequence text) {
            return text.toString().getBytes(StandardCharsets.UTF_8);
        }
        try {
            // REST responses are JSON by default in this module. Using the same
            // Jackson API available to the response providers preserves POJO
            // structure instead of persisting Object#toString().
            return JSON.writeValueAsBytes(entity);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Idempotent response is not serializable", e);
        }
    }

    private static Map<String, List<String>> copyHeaders(ContainerResponseContext responseContext) {
        Map<String, List<String>> copy = new LinkedHashMap<>();
        MultivaluedMap<String, Object> headers = responseContext.getHeaders();
        if (headers != null) {
            headers.forEach((name, values) -> {
                List<String> asStrings = new ArrayList<>();
                for (Object v : values) {
                    asStrings.add(String.valueOf(v));
                }
                copy.put(name, asStrings);
            });
        }
        return copy;
    }

    private static Response replay(IdempotencyRecord record) {
        Response.ResponseBuilder builder = Response.status(record.httpStatus());
        record.headers().forEach((name, values) -> {
            for (String v : values) {
                builder.header(name, v);
            }
        });
        byte[] body = record.body();
        if (body != null) {
            // byte[] has a standard JAX-RS MessageBodyWriter and preserves the
            // exact representation captured on the first successful response,
            // including non-text media types.
            builder.entity(body.clone());
        }
        builder.header(HEADER_REPLAYED, "true");
        return builder.build();
    }
}
