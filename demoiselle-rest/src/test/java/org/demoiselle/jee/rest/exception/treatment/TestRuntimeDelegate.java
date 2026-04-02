/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

import java.util.LinkedHashMap;
import java.util.Map;

import jakarta.ws.rs.core.Application;
import jakarta.ws.rs.core.EntityPart;
import jakarta.ws.rs.core.Link;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriBuilder;
import jakarta.ws.rs.core.Variant;
import jakarta.ws.rs.ext.RuntimeDelegate;

/**
 * Minimal JAX-RS RuntimeDelegate for unit testing without a full JAX-RS runtime.
 * Supports Response.status() and MediaType.valueOf() which are needed by ExceptionTreatmentImpl.
 */
public class TestRuntimeDelegate extends RuntimeDelegate {

    public static void install() {
        RuntimeDelegate.setInstance(new TestRuntimeDelegate());
    }

    @Override
    public UriBuilder createUriBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Response.ResponseBuilder createResponseBuilder() {
        return new TestResponseBuilder();
    }

    @Override
    public Variant.VariantListBuilder createVariantListBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T createEndpoint(Application application, Class<T> endpointType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> HeaderDelegate<T> createHeaderDelegate(Class<T> type) {
        if (type == MediaType.class) {
            @SuppressWarnings("unchecked")
            HeaderDelegate<T> delegate = (HeaderDelegate<T>) new MediaTypeHeaderDelegate();
            return delegate;
        }
        throw new UnsupportedOperationException("No header delegate for " + type);
    }

    @Override
    public Link.Builder createLinkBuilder() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityPart.Builder createEntityPartBuilder(String partName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.concurrent.CompletionStage<jakarta.ws.rs.SeBootstrap.Instance> bootstrap(
            Class<? extends Application> clazz, jakarta.ws.rs.SeBootstrap.Configuration configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public java.util.concurrent.CompletionStage<jakarta.ws.rs.SeBootstrap.Instance> bootstrap(
            Application application, jakarta.ws.rs.SeBootstrap.Configuration configuration) {
        throw new UnsupportedOperationException();
    }

    @Override
    public jakarta.ws.rs.SeBootstrap.Configuration.Builder createConfigurationBuilder() {
        throw new UnsupportedOperationException();
    }

    // ── MediaType header delegate ──────────────────────────────────

    private static class MediaTypeHeaderDelegate implements HeaderDelegate<MediaType> {
        @Override
        public MediaType fromString(String value) {
            if (value == null) return null;
            String[] parts = value.split("/", 2);
            if (parts.length == 2) {
                // Handle parameters like charset
                String subtype = parts[1];
                Map<String, String> params = new LinkedHashMap<>();
                if (subtype.contains(";")) {
                    String[] subParts = subtype.split(";", 2);
                    subtype = subParts[0].trim();
                    // Simple param parsing
                    String[] paramPairs = subParts[1].trim().split(";");
                    for (String pair : paramPairs) {
                        String[] kv = pair.trim().split("=", 2);
                        if (kv.length == 2) {
                            params.put(kv[0].trim(), kv[1].trim());
                        }
                    }
                }
                return new MediaType(parts[0].trim(), subtype.trim(), params);
            }
            return new MediaType(value, null);
        }

        @Override
        public String toString(MediaType value) {
            if (value == null) return null;
            return value.getType() + "/" + value.getSubtype();
        }
    }

    // ── Test Response Builder ──────────────────────────────────────

    private static class TestResponseBuilder extends Response.ResponseBuilder {
        private int status;
        private Object entity;
        private MediaType mediaType;

        @Override
        public Response build() {
            return new TestResponse(status, entity, mediaType);
        }

        @Override
        public Response.ResponseBuilder clone() {
            TestResponseBuilder clone = new TestResponseBuilder();
            clone.status = this.status;
            clone.entity = this.entity;
            clone.mediaType = this.mediaType;
            return clone;
        }

        @Override
        public Response.ResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        @Override
        public Response.ResponseBuilder status(int status, String reasonPhrase) {
            this.status = status;
            return this;
        }

        // Override to avoid NPE when Status.fromStatusCode() returns null
        public Response.ResponseBuilder status(Response.StatusType statusInfo) {
            if (statusInfo != null) {
                this.status = statusInfo.getStatusCode();
            }
            return this;
        }

        @Override
        public Response.ResponseBuilder entity(Object entity) {
            this.entity = entity;
            return this;
        }

        @Override
        public Response.ResponseBuilder entity(Object entity, java.lang.annotation.Annotation[] annotations) {
            this.entity = entity;
            return this;
        }

        @Override
        public Response.ResponseBuilder allow(String... methods) { return this; }

        @Override
        public Response.ResponseBuilder allow(java.util.Set<String> methods) { return this; }

        @Override
        public Response.ResponseBuilder cacheControl(jakarta.ws.rs.core.CacheControl cacheControl) { return this; }

        @Override
        public Response.ResponseBuilder encoding(String encoding) { return this; }

        @Override
        public Response.ResponseBuilder header(String name, Object value) { return this; }

        @Override
        public Response.ResponseBuilder replaceAll(jakarta.ws.rs.core.MultivaluedMap<String, Object> headers) { return this; }

        @Override
        public Response.ResponseBuilder language(String language) { return this; }

        @Override
        public Response.ResponseBuilder language(java.util.Locale language) { return this; }

        @Override
        public Response.ResponseBuilder type(MediaType type) {
            this.mediaType = type;
            return this;
        }

        @Override
        public Response.ResponseBuilder type(String type) {
            if (type != null) {
                this.mediaType = MediaType.valueOf(type);
            }
            return this;
        }

        @Override
        public Response.ResponseBuilder variant(Variant variant) { return this; }

        @Override
        public Response.ResponseBuilder contentLocation(java.net.URI location) { return this; }

        @Override
        public Response.ResponseBuilder cookie(jakarta.ws.rs.core.NewCookie... cookies) { return this; }

        @Override
        public Response.ResponseBuilder expires(java.util.Date expires) { return this; }

        @Override
        public Response.ResponseBuilder lastModified(java.util.Date lastModified) { return this; }

        @Override
        public Response.ResponseBuilder location(java.net.URI location) { return this; }

        @Override
        public Response.ResponseBuilder tag(jakarta.ws.rs.core.EntityTag tag) { return this; }

        @Override
        public Response.ResponseBuilder tag(String tag) { return this; }

        @Override
        public Response.ResponseBuilder variants(Variant... variants) { return this; }

        @Override
        public Response.ResponseBuilder variants(java.util.List<Variant> variants) { return this; }

        @Override
        public Response.ResponseBuilder links(Link... links) { return this; }

        @Override
        public Response.ResponseBuilder link(java.net.URI uri, String rel) { return this; }

        @Override
        public Response.ResponseBuilder link(String uri, String rel) { return this; }
    }

    // ── Test Response ──────────────────────────────────────────────

    private static class TestResponse extends Response {
        private final int status;
        private final Object entity;
        private final MediaType mediaType;

        TestResponse(int status, Object entity, MediaType mediaType) {
            this.status = status;
            this.entity = entity;
            this.mediaType = mediaType;
        }

        @Override
        public int getStatus() { return status; }

        @Override
        public StatusType getStatusInfo() {
            return Response.Status.fromStatusCode(status);
        }

        @Override
        public Object getEntity() { return entity; }

        @Override
        public <T> T readEntity(Class<T> entityType) { throw new UnsupportedOperationException(); }

        @Override
        public <T> T readEntity(jakarta.ws.rs.core.GenericType<T> entityType) { throw new UnsupportedOperationException(); }

        @Override
        public <T> T readEntity(Class<T> entityType, java.lang.annotation.Annotation[] annotations) { throw new UnsupportedOperationException(); }

        @Override
        public <T> T readEntity(jakarta.ws.rs.core.GenericType<T> entityType, java.lang.annotation.Annotation[] annotations) { throw new UnsupportedOperationException(); }

        @Override
        public boolean hasEntity() { return entity != null; }

        @Override
        public boolean bufferEntity() { return false; }

        @Override
        public void close() {}

        @Override
        public MediaType getMediaType() { return mediaType; }

        @Override
        public java.util.Locale getLanguage() { return null; }

        @Override
        public int getLength() { return -1; }

        @Override
        public java.util.Set<String> getAllowedMethods() { return java.util.Set.of(); }

        @Override
        public java.util.Map<String, jakarta.ws.rs.core.NewCookie> getCookies() { return java.util.Map.of(); }

        @Override
        public jakarta.ws.rs.core.EntityTag getEntityTag() { return null; }

        @Override
        public java.util.Date getDate() { return null; }

        @Override
        public java.util.Date getLastModified() { return null; }

        @Override
        public java.net.URI getLocation() { return null; }

        @Override
        public java.util.Set<Link> getLinks() { return java.util.Set.of(); }

        @Override
        public boolean hasLink(String relation) { return false; }

        @Override
        public Link getLink(String relation) { return null; }

        @Override
        public Link.Builder getLinkBuilder(String relation) { return null; }

        @Override
        public jakarta.ws.rs.core.MultivaluedMap<String, Object> getMetadata() {
            return new jakarta.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public jakarta.ws.rs.core.MultivaluedMap<String, Object> getHeaders() {
            return new jakarta.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public jakarta.ws.rs.core.MultivaluedMap<String, String> getStringHeaders() {
            return new jakarta.ws.rs.core.MultivaluedHashMap<>();
        }

        @Override
        public String getHeaderString(String name) { return null; }
    }
}
