/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.RuntimeDelegate;

import java.lang.annotation.Annotation;

/**
 * Minimal in-memory {@link RuntimeDelegate} so tests can build {@link Response}
 * objects without a full JAX-RS runtime on the classpath. Only the pieces used
 * by {@link McpSseTransport} (status, entity, headers, media type) are
 * implemented.
 */
final class TestRuntimeDelegate extends RuntimeDelegate {

    static void install() {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @Override
    public Response.ResponseBuilder createResponseBuilder() {
        return new StubResponseBuilder();
    }

    @Override
    public jakarta.ws.rs.core.UriBuilder createUriBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public jakarta.ws.rs.core.Variant.VariantListBuilder createVariantListBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T createEndpoint(jakarta.ws.rs.core.Application application, Class<T> endpointType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
        return new HeaderDelegate<>() {
            @Override public T fromString(String value) { return null; }
            @Override public String toString(T value) { return String.valueOf(value); }
        };
    }

    @Override
    public jakarta.ws.rs.core.Link.Builder createLinkBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public jakarta.ws.rs.core.EntityPart.Builder createEntityPartBuilder(String partName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public jakarta.ws.rs.SeBootstrap.Configuration.Builder createConfigurationBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.concurrent.CompletionStage<jakarta.ws.rs.SeBootstrap.Instance> bootstrap(
            jakarta.ws.rs.core.Application application, jakarta.ws.rs.SeBootstrap.Configuration configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.concurrent.CompletionStage<jakarta.ws.rs.SeBootstrap.Instance> bootstrap(
            Class<? extends jakarta.ws.rs.core.Application> clazz,
            jakarta.ws.rs.SeBootstrap.Configuration configuration) {
        throw new UnsupportedOperationException();
    }

    static final class StubResponseBuilder extends Response.ResponseBuilder {
        private int status = 200;
        private Object entity;
        private MediaType mediaType;
        private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        @Override public Response build() {
            return new StubResponse(status, entity, mediaType, headers);
        }
        @Override public Response.ResponseBuilder clone() { return this; }
        @Override public Response.ResponseBuilder status(int status) { this.status = status; return this; }
        @Override public Response.ResponseBuilder status(int status, String reasonPhrase) { this.status = status; return this; }
        @Override public Response.ResponseBuilder entity(Object entity) { this.entity = entity; return this; }
        @Override public Response.ResponseBuilder entity(Object entity, Annotation[] annotations) { this.entity = entity; return this; }
        @Override public Response.ResponseBuilder allow(String... methods) { return this; }
        @Override public Response.ResponseBuilder allow(java.util.Set<String> methods) { return this; }
        @Override public Response.ResponseBuilder cacheControl(jakarta.ws.rs.core.CacheControl cacheControl) { return this; }
        @Override public Response.ResponseBuilder encoding(String encoding) { return this; }
        @Override public Response.ResponseBuilder header(String name, Object value) { headers.add(name, value); return this; }
        @Override public Response.ResponseBuilder replaceAll(MultivaluedMap<String, Object> headers) { return this; }
        @Override public Response.ResponseBuilder language(String language) { return this; }
        @Override public Response.ResponseBuilder language(java.util.Locale language) { return this; }
        @Override public Response.ResponseBuilder type(MediaType type) { this.mediaType = type; return this; }
        @Override public Response.ResponseBuilder type(String type) { this.mediaType = MediaType.valueOf(type); return this; }
        @Override public Response.ResponseBuilder variant(jakarta.ws.rs.core.Variant variant) { return this; }
        @Override public Response.ResponseBuilder contentLocation(java.net.URI location) { return this; }
        @Override public Response.ResponseBuilder cookie(jakarta.ws.rs.core.NewCookie... cookies) { return this; }
        @Override public Response.ResponseBuilder expires(java.util.Date expires) { return this; }
        @Override public Response.ResponseBuilder lastModified(java.util.Date lastModified) { return this; }
        @Override public Response.ResponseBuilder location(java.net.URI location) { return this; }
        @Override public Response.ResponseBuilder tag(jakarta.ws.rs.core.EntityTag tag) { return this; }
        @Override public Response.ResponseBuilder tag(String tag) { return this; }
        @Override public Response.ResponseBuilder variants(jakarta.ws.rs.core.Variant... variants) { return this; }
        @Override public Response.ResponseBuilder variants(java.util.List<jakarta.ws.rs.core.Variant> variants) { return this; }
        @Override public Response.ResponseBuilder links(jakarta.ws.rs.core.Link... links) { return this; }
        @Override public Response.ResponseBuilder link(java.net.URI uri, String rel) { return this; }
        @Override public Response.ResponseBuilder link(String uri, String rel) { return this; }
    }

    static final class StubResponse extends Response {
        private final int status;
        private final Object entity;
        private final MediaType mediaType;
        private final MultivaluedMap<String, Object> headers;

        StubResponse(int status, Object entity, MediaType mediaType, MultivaluedMap<String, Object> headers) {
            this.status = status;
            this.entity = entity;
            this.mediaType = mediaType;
            this.headers = headers;
        }

        @Override public int getStatus() { return status; }
        @Override public StatusType getStatusInfo() { return Status.fromStatusCode(status); }
        @Override public Object getEntity() { return entity; }
        @Override public <T> T readEntity(Class<T> entityType) { return null; }
        @Override public <T> T readEntity(jakarta.ws.rs.core.GenericType<T> entityType) { return null; }
        @Override public <T> T readEntity(Class<T> entityType, Annotation[] annotations) { return null; }
        @Override public <T> T readEntity(jakarta.ws.rs.core.GenericType<T> entityType, Annotation[] annotations) { return null; }
        @Override public boolean hasEntity() { return entity != null; }
        @Override public boolean bufferEntity() { return false; }
        @Override public void close() { }
        @Override public MediaType getMediaType() { return mediaType; }
        @Override public java.util.Locale getLanguage() { return null; }
        @Override public int getLength() { return -1; }
        @Override public java.util.Set<String> getAllowedMethods() { return java.util.Set.of(); }
        @Override public java.util.Map<String, jakarta.ws.rs.core.NewCookie> getCookies() { return java.util.Map.of(); }
        @Override public jakarta.ws.rs.core.EntityTag getEntityTag() { return null; }
        @Override public java.util.Date getDate() { return null; }
        @Override public java.util.Date getLastModified() { return null; }
        @Override public java.net.URI getLocation() { return null; }
        @Override public java.util.Set<jakarta.ws.rs.core.Link> getLinks() { return java.util.Set.of(); }
        @Override public boolean hasLink(String relation) { return false; }
        @Override public jakarta.ws.rs.core.Link getLink(String relation) { return null; }
        @Override public jakarta.ws.rs.core.Link.Builder getLinkBuilder(String relation) { return null; }
        @Override public MultivaluedMap<String, Object> getMetadata() { return headers; }
        @Override public MultivaluedMap<String, String> getStringHeaders() {
            MultivaluedMap<String, String> m = new MultivaluedHashMap<>();
            headers.forEach((k, v) -> v.forEach(o -> m.add(k, String.valueOf(o))));
            return m;
        }
        @Override public String getHeaderString(String name) {
            Object v = headers.getFirst(name);
            return v == null ? null : String.valueOf(v);
        }
    }
}
