/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.security.filter;

import net.jqwik.api.*;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ResourceInfo;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

import org.demoiselle.jee.security.DemoiselleSecurityConfig;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

// Feature: security-enhancements, Property 10: Validação de origem CORS
/**
 * Property-based tests for {@link CorsFilter} CORS origin validation.
 *
 * <p><b>Validates: Requirements 5.2, 5.3, 5.4</b></p>
 */
class CorsFilterPropertyTest {

    // ---- Stubs ----

    /**
     * Stub DemoiselleSecurityConfig with configurable CORS properties.
     */
    static class StubSecurityConfig extends DemoiselleSecurityConfig {
        private final List<String> allowedOrigins;
        private final List<String> allowedMethods;
        private final List<String> allowedHeaders;
        private final int maxAge;

        StubSecurityConfig(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
            this.allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS");
            this.allowedHeaders = List.of("Content-Type", "Authorization");
            this.maxAge = 3600;
        }

        @Override public boolean isCorsEnabled() { return true; }
        @Override public List<String> getCorsAllowedOrigins() { return allowedOrigins; }
        @Override public List<String> getCorsAllowedMethods() { return allowedMethods; }
        @Override public List<String> getCorsAllowedHeaders() { return allowedHeaders; }
        @Override public int getCorsMaxAge() { return maxAge; }
        @Override public Map<String, String> getParamsHeaderSecuriry() { return Map.of(); }
        @Override public Map<String, String> getParamsHeaderCors() { return Map.of(); }
    }

    /**
     * Stub ContainerRequestContext that returns a configurable Origin header.
     */
    static class StubRequestContext implements ContainerRequestContext {
        private final String origin;

        StubRequestContext(String origin) { this.origin = origin; }

        @Override public String getHeaderString(String name) {
            return "Origin".equalsIgnoreCase(name) ? origin : null;
        }

        // Minimal stubs for unused methods
        @Override public Object getProperty(String name) { return null; }
        @Override public Collection<String> getPropertyNames() { return List.of(); }
        @Override public void setProperty(String name, Object object) {}
        @Override public void removeProperty(String name) {}
        @Override public jakarta.ws.rs.core.UriInfo getUriInfo() { return null; }
        @Override public void setRequestUri(java.net.URI requestUri) {}
        @Override public void setRequestUri(java.net.URI baseUri, java.net.URI requestUri) {}
        @Override public jakarta.ws.rs.core.Request getRequest() { return null; }
        @Override public String getMethod() { return "GET"; }
        @Override public void setMethod(String method) {}
        @Override public MultivaluedMap<String, String> getHeaders() { return new MultivaluedHashMap<>(); }
        @Override public Date getDate() { return null; }
        @Override public java.util.Locale getLanguage() { return null; }
        @Override public int getLength() { return 0; }
        @Override public jakarta.ws.rs.core.MediaType getMediaType() { return null; }
        @Override public List<jakarta.ws.rs.core.MediaType> getAcceptableMediaTypes() { return List.of(); }
        @Override public List<java.util.Locale> getAcceptableLanguages() { return List.of(); }
        @Override public Map<String, jakarta.ws.rs.core.Cookie> getCookies() { return Map.of(); }
        @Override public boolean hasEntity() { return false; }
        @Override public java.io.InputStream getEntityStream() { return null; }
        @Override public void setEntityStream(java.io.InputStream input) {}
        @Override public jakarta.ws.rs.core.SecurityContext getSecurityContext() { return null; }
        @Override public void setSecurityContext(jakarta.ws.rs.core.SecurityContext context) {}
        @Override public void abortWith(jakarta.ws.rs.core.Response response) {}
    }

    /**
     * Stub ContainerResponseContext backed by a real MultivaluedHashMap for headers.
     */
    static class StubResponseContext implements ContainerResponseContext {
        private final MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

        @Override public MultivaluedMap<String, Object> getHeaders() { return headers; }
        @Override public int getStatus() { return 200; }
        @Override public void setStatus(int code) {}
        @Override public jakarta.ws.rs.core.Response.StatusType getStatusInfo() { return null; }
        @Override public void setStatusInfo(jakarta.ws.rs.core.Response.StatusType statusInfo) {}
        @Override public Class<?> getEntityClass() { return null; }
        @Override public java.lang.reflect.Type getEntityType() { return null; }
        @Override public void setEntity(Object entity) {}
        @Override public void setEntity(Object entity, java.lang.annotation.Annotation[] annotations, jakarta.ws.rs.core.MediaType mediaType) {}
        @Override public java.lang.annotation.Annotation[] getEntityAnnotations() { return new java.lang.annotation.Annotation[0]; }
        @Override public java.io.OutputStream getEntityStream() { return null; }
        @Override public void setEntityStream(java.io.OutputStream outputStream) {}
        @Override public int getLength() { return 0; }
        @Override public jakarta.ws.rs.core.MediaType getMediaType() { return null; }
        @Override public Map<String, jakarta.ws.rs.core.NewCookie> getCookies() { return Map.of(); }
        @Override public MultivaluedMap<String, String> getStringHeaders() { return new MultivaluedHashMap<>(); }
        @Override public String getHeaderString(String name) { return null; }
        @Override public boolean hasEntity() { return false; }
        @Override public Object getEntity() { return null; }
        @Override public java.util.Locale getLanguage() { return null; }
        @Override public Date getDate() { return null; }
        @Override public java.net.URI getLocation() { return null; }
        @Override public Set<String> getAllowedMethods() { return Set.of(); }
        @Override public jakarta.ws.rs.core.EntityTag getEntityTag() { return null; }
        @Override public Date getLastModified() { return null; }
        @Override public Set<jakarta.ws.rs.core.Link> getLinks() { return Set.of(); }
        @Override public boolean hasLink(String relation) { return false; }
        @Override public jakarta.ws.rs.core.Link getLink(String relation) { return null; }
        @Override public jakarta.ws.rs.core.Link.Builder getLinkBuilder(String relation) { return null; }
    }

    /**
     * Stub ResourceInfo that returns null for both method and class (no @Cors annotation).
     */
    static class StubResourceInfo implements ResourceInfo {
        @Override public Method getResourceMethod() { return null; }
        @Override public Class<?> getResourceClass() { return null; }
    }

    // ---- Helper ----

    private CorsFilter createFilter(DemoiselleSecurityConfig config) throws Exception {
        CorsFilter filter = new CorsFilter();

        Field configField = CorsFilter.class.getDeclaredField("config");
        configField.setAccessible(true);
        configField.set(filter, config);

        Field infoField = CorsFilter.class.getDeclaredField("info");
        infoField.setAccessible(true);
        infoField.set(filter, new StubResourceInfo());

        return filter;
    }

    // ---- Property Test ----

    // Feature: security-enhancements, Property 10: Validação de origem CORS
    /**
     * Property 10: For any request with an Origin header and any list of allowedOrigins
     * configured, the CorsFilter must include CORS headers in the response if and only if
     * the origin is in the allowed list (or the list contains "*"). If the origin is not
     * in the list, CORS headers must be omitted.
     *
     * <p><b>Validates: Requirements 5.2, 5.3, 5.4</b></p>
     */
    @Property(tries = 100)
    void corsHeadersIncludedIffOriginAllowed(
            @ForAll("origins") String requestOrigin,
            @ForAll("allowedOriginLists") List<String> allowedOrigins
    ) throws Exception {
        StubSecurityConfig config = new StubSecurityConfig(allowedOrigins);
        CorsFilter filter = createFilter(config);

        StubRequestContext req = new StubRequestContext(requestOrigin);
        StubResponseContext res = new StubResponseContext();

        filter.filter(req, res);

        boolean isWildcard = allowedOrigins.contains("*");
        boolean isExplicitlyAllowed = allowedOrigins.contains(requestOrigin);
        boolean shouldIncludeCors = isWildcard || isExplicitlyAllowed;

        MultivaluedMap<String, Object> headers = res.getHeaders();

        if (shouldIncludeCors) {
            // CORS headers must be present
            assertNotNull(headers.getFirst("Access-Control-Allow-Origin"),
                    "Access-Control-Allow-Origin must be present when origin is allowed. " +
                    "Origin: " + requestOrigin + ", Allowed: " + allowedOrigins);

            if (isWildcard) {
                assertEquals("*", headers.getFirst("Access-Control-Allow-Origin"),
                        "Allow-Origin must be '*' when wildcard is configured");
            } else {
                assertEquals(requestOrigin, headers.getFirst("Access-Control-Allow-Origin"),
                        "Allow-Origin must echo the request origin when explicitly allowed");
            }

            assertNotNull(headers.getFirst("Access-Control-Allow-Methods"),
                    "Access-Control-Allow-Methods must be present when origin is allowed");
            assertNotNull(headers.getFirst("Access-Control-Allow-Headers"),
                    "Access-Control-Allow-Headers must be present when origin is allowed");
            assertNotNull(headers.getFirst("Access-Control-Max-Age"),
                    "Access-Control-Max-Age must be present when origin is allowed");
        } else {
            // CORS headers must be absent
            assertNull(headers.getFirst("Access-Control-Allow-Origin"),
                    "Access-Control-Allow-Origin must be absent when origin is NOT allowed. " +
                    "Origin: " + requestOrigin + ", Allowed: " + allowedOrigins);
            assertNull(headers.getFirst("Access-Control-Allow-Methods"),
                    "Access-Control-Allow-Methods must be absent when origin is NOT allowed");
            assertNull(headers.getFirst("Access-Control-Allow-Headers"),
                    "Access-Control-Allow-Headers must be absent when origin is NOT allowed");
            assertNull(headers.getFirst("Access-Control-Max-Age"),
                    "Access-Control-Max-Age must be absent when origin is NOT allowed");
        }
    }

    // ---- Providers ----

    /**
     * Generates random origin strings (scheme + host patterns).
     */
    @Provide
    Arbitrary<String> origins() {
        Arbitrary<String> schemes = Arbitraries.of("http", "https");
        Arbitrary<String> hosts = Arbitraries.of(
                "example.com", "app.example.com", "localhost",
                "mysite.org", "api.internal.net", "other.io"
        );
        Arbitrary<Integer> ports = Arbitraries.of(80, 443, 3000, 8080, 9090);

        return Combinators.combine(schemes, hosts, ports)
                .as((scheme, host, port) -> scheme + "://" + host + ":" + port);
    }

    /**
     * Generates random lists of allowed origins. Includes scenarios with wildcard "*",
     * specific origins, and lists that may or may not contain the request origin.
     */
    @Provide
    Arbitrary<List<String>> allowedOriginLists() {
        Arbitrary<String> specificOrigins = Arbitraries.of(
                "http://example.com:80", "https://example.com:443",
                "http://localhost:3000", "https://app.example.com:8080",
                "http://mysite.org:9090", "https://api.internal.net:443"
        );

        Arbitrary<List<String>> specificLists = specificOrigins.list().ofMinSize(1).ofMaxSize(5);
        Arbitrary<List<String>> wildcardList = Arbitraries.just(List.of("*"));

        // Mix: ~30% wildcard, ~70% specific origins
        return Arbitraries.frequencyOf(
                Tuple.of(3, wildcardList),
                Tuple.of(7, specificLists)
        );
    }
}
