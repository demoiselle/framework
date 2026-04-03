/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.handler.McpJsonRpcHandler;
import org.demoiselle.jee.mcp.integration.JwtValidationResult;
import org.demoiselle.jee.mcp.integration.JwtValidator;
import org.demoiselle.jee.mcp.integration.NoOpJwtValidator;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for JWT authentication in {@link McpSseTransport}.
 *
 * Validates: Requirements 14.1, 14.2, 14.3, 14.4, 14.5
 */
class McpSseTransportTest {

    private McpSseTransport transport;
    private McpConfig config;
    private StubSseEventSink sink;
    private StubSse sse;
    private StubHttpHeaders headers;

    @BeforeEach
    void setUp() throws Exception {
        transport = new McpSseTransport();
        config = new McpConfig();
        sink = new StubSseEventSink();
        sse = new StubSse();
        headers = new StubHttpHeaders();

        setField(transport, "config", config);
        setField(transport, "handler", createStubHandler());
        setField(transport, "serializer", new JsonRpcSerializer());
        setField(transport, "jwtValidator", new NoOpJwtValidator());
    }

    // --- Requirement 14.4: security disabled allows unauthenticated connections ---

    @Test
    void connect_securityDisabled_allowsWithoutToken() {
        config.setSecurityEnabled(false);

        transport.connect(sink, sse, headers);

        assertFalse(sink.closed, "Sink should not be closed when security is disabled");
        assertEquals(1, sink.eventCount, "Should send endpoint event");
        assertEquals(1, getSessionCount());
    }

    @Test
    void connect_securityDisabled_allowsWithToken() {
        config.setSecurityEnabled(false);
        headers.authorizationHeader = "Bearer some.jwt.token";

        transport.connect(sink, sse, headers);

        assertFalse(sink.closed);
        assertEquals(1, sink.eventCount);
    }

    // --- Requirement 14.1 & 14.2: security enabled requires valid JWT ---

    @Test
    void connect_securityEnabled_noAuthHeader_returns401() {
        config.setSecurityEnabled(true);
        headers.authorizationHeader = null;

        transport.connect(sink, sse, headers);

        assertTrue(sink.closed, "Sink should be closed on auth failure");
        assertTrue(sink.lastEventData != null && sink.lastEventData.contains("401"));
        assertEquals(0, getSessionCount(), "No session should be created");
    }

    @Test
    void connect_securityEnabled_emptyBearer_returns401() {
        config.setSecurityEnabled(true);
        headers.authorizationHeader = "Bearer ";

        transport.connect(sink, sse, headers);

        assertTrue(sink.closed);
        assertEquals(0, getSessionCount());
    }

    @Test
    void connect_securityEnabled_noBearerPrefix_returns401() {
        config.setSecurityEnabled(true);
        headers.authorizationHeader = "Basic dXNlcjpwYXNz";

        transport.connect(sink, sse, headers);

        assertTrue(sink.closed);
        assertEquals(0, getSessionCount());
    }

    @Test
    void connect_securityEnabled_invalidToken_returns401() {
        config.setSecurityEnabled(true);
        headers.authorizationHeader = "Bearer invalid.token.here";
        setField(transport, "jwtValidator", (JwtValidator) token ->
                JwtValidationResult.invalid("Bad signature"));

        transport.connect(sink, sse, headers);

        assertTrue(sink.closed);
        assertTrue(sink.lastEventData.contains("Invalid JWT token"));
        assertEquals(0, getSessionCount());
    }

    // --- Requirement 14.3: expired token returns 401 with "Token expired" ---

    @Test
    void connect_securityEnabled_expiredToken_returns401WithTokenExpired() {
        config.setSecurityEnabled(true);
        headers.authorizationHeader = "Bearer expired.token.here";
        setField(transport, "jwtValidator", (JwtValidator) token ->
                JwtValidationResult.tokenExpired());

        transport.connect(sink, sse, headers);

        assertTrue(sink.closed);
        assertTrue(sink.lastEventData.contains("Token expired"),
                "Error detail should contain 'Token expired', got: " + sink.lastEventData);
        assertEquals(0, getSessionCount());
    }

    // --- Requirement 14.1: valid token allows connection ---

    @Test
    void connect_securityEnabled_validToken_allowsConnection() {
        config.setSecurityEnabled(true);
        headers.authorizationHeader = "Bearer valid.jwt.token";
        setField(transport, "jwtValidator", new NoOpJwtValidator());

        transport.connect(sink, sse, headers);

        assertFalse(sink.closed);
        assertEquals(1, sink.eventCount);
        assertEquals(1, getSessionCount());
    }

    // --- NoOpJwtValidator always returns valid ---

    @Test
    void noOpJwtValidator_alwaysReturnsValid() {
        NoOpJwtValidator validator = new NoOpJwtValidator();
        JwtValidationResult result = validator.validate("any-token");
        assertTrue(result.valid());
        assertFalse(result.expired());
        assertNull(result.detail());
    }

    // --- JwtValidationResult factory methods ---

    @Test
    void jwtValidationResult_ok() {
        JwtValidationResult result = JwtValidationResult.ok();
        assertTrue(result.valid());
        assertFalse(result.expired());
        assertNull(result.detail());
    }

    @Test
    void jwtValidationResult_invalid() {
        JwtValidationResult result = JwtValidationResult.invalid("bad sig");
        assertFalse(result.valid());
        assertFalse(result.expired());
        assertEquals("bad sig", result.detail());
    }

    @Test
    void jwtValidationResult_expired() {
        JwtValidationResult result = JwtValidationResult.tokenExpired();
        assertFalse(result.valid());
        assertTrue(result.expired());
        assertEquals("Token expired", result.detail());
    }

    // --- Helpers ---

    @SuppressWarnings("unchecked")
    private int getSessionCount() {
        try {
            Field f = McpSseTransport.class.getDeclaredField("sessions");
            f.setAccessible(true);
            return ((ConcurrentHashMap<String, McpSession>) f.get(transport)).size();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void setField(Object target, String fieldName, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    // --- Stub implementations ---

    /** Minimal handler stub — we only need it injected, not actually called for auth tests. */
    private static McpJsonRpcHandler createStubHandler() {
        return new McpJsonRpcHandler(
                new org.demoiselle.jee.mcp.registry.McpToolRegistry(),
                new org.demoiselle.jee.mcp.registry.McpResourceRegistry(),
                new org.demoiselle.jee.mcp.registry.McpPromptRegistry(),
                new JsonRpcSerializer(),
                new McpConfig(),
                new org.demoiselle.jee.mcp.integration.PlainTextErrorFormatter()
        );
    }

    private static class StubHttpHeaders implements HttpHeaders {
        String authorizationHeader;

        @Override
        public String getHeaderString(String name) {
            if (HttpHeaders.AUTHORIZATION.equalsIgnoreCase(name)) {
                return authorizationHeader;
            }
            return null;
        }

        // Remaining HttpHeaders methods — not needed for these tests
        @Override public java.util.List<String> getRequestHeader(String name) { return java.util.List.of(); }
        @Override public jakarta.ws.rs.core.MultivaluedMap<String, String> getRequestHeaders() { return new jakarta.ws.rs.core.MultivaluedHashMap<>(); }
        @Override public java.util.List<jakarta.ws.rs.core.MediaType> getAcceptableMediaTypes() { return java.util.List.of(); }
        @Override public java.util.List<java.util.Locale> getAcceptableLanguages() { return java.util.List.of(); }
        @Override public jakarta.ws.rs.core.MediaType getMediaType() { return null; }
        @Override public java.util.Locale getLanguage() { return null; }
        @Override public java.util.Map<String, jakarta.ws.rs.core.Cookie> getCookies() { return java.util.Map.of(); }
        @Override public java.util.Date getDate() { return null; }
        @Override public int getLength() { return 0; }
    }

    private static class StubSseEventSink implements SseEventSink {
        boolean closed = false;
        int eventCount = 0;
        String lastEventData = null;

        @Override
        public CompletionStage<?> send(OutboundSseEvent event) {
            eventCount++;
            lastEventData = event.getData() != null ? event.getData().toString() : null;
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public boolean isClosed() {
            return closed;
        }

        @Override
        public void close() {
            closed = true;
        }
    }

    private static class StubSse implements Sse {
        @Override
        public OutboundSseEvent.Builder newEventBuilder() {
            return new StubOutboundSseEventBuilder();
        }

        @Override
        public jakarta.ws.rs.sse.SseBroadcaster newBroadcaster() {
            return null;
        }
    }

    private static class StubOutboundSseEventBuilder implements OutboundSseEvent.Builder {
        private String name;
        private Object data;

        @Override public OutboundSseEvent.Builder id(String id) { return this; }
        @Override public OutboundSseEvent.Builder name(String name) { this.name = name; return this; }
        @Override public OutboundSseEvent.Builder reconnectDelay(long ms) { return this; }
        @Override public OutboundSseEvent.Builder mediaType(jakarta.ws.rs.core.MediaType mt) { return this; }
        @Override public OutboundSseEvent.Builder comment(String comment) { return this; }
        @Override public OutboundSseEvent.Builder data(Class type, Object data) { this.data = data; return this; }
        @Override public OutboundSseEvent.Builder data(jakarta.ws.rs.core.GenericType type, Object data) { this.data = data; return this; }
        @Override public OutboundSseEvent.Builder data(Object data) { this.data = data; return this; }

        @Override
        public OutboundSseEvent build() {
            return new StubOutboundSseEvent(name, data);
        }
    }

    private static class StubOutboundSseEvent implements OutboundSseEvent {
        private final String name;
        private final Object data;

        StubOutboundSseEvent(String name, Object data) {
            this.name = name;
            this.data = data;
        }

        @Override public String getName() { return name; }
        @Override public String getId() { return null; }
        @Override public String getComment() { return null; }
        @Override public long getReconnectDelay() { return -1; }
        @Override public boolean isReconnectDelaySet() { return false; }
        @Override public Class<?> getType() { return data != null ? data.getClass() : Object.class; }
        @Override public java.lang.reflect.Type getGenericType() { return data != null ? data.getClass() : Object.class; }
        @Override public jakarta.ws.rs.core.MediaType getMediaType() { return null; }
        @Override public Object getData() { return data; }
    }
}
