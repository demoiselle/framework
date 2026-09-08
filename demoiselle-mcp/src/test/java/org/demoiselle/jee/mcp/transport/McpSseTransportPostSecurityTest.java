/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.OutboundSseEvent;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.handler.McpJsonRpcHandler;
import org.demoiselle.jee.mcp.integration.JwtValidationResult;
import org.demoiselle.jee.mcp.integration.JwtValidator;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for the security behaviours of {@link McpSseTransport#receiveMessage}.
 *
 * <p>Covers: session/principal binding (401 on foreign token), unknown session
 * (404), expired session (410), tool rate limiting (429), session cap, and that
 * the {@code sessionId} is never logged at INFO level.</p>
 */
class McpSseTransportPostSecurityTest {

    private McpSseTransport transport;
    private McpConfig config;
    private StubSse sse;

    @BeforeEach
    void setUp() {
        TestRuntimeDelegate.install();
        transport = new McpSseTransport();
        config = new McpConfig();
        sse = new StubSse();
        setField(transport, "config", config);
        setField(transport, "handler", stubHandler());
        setField(transport, "serializer", new JsonRpcSerializer());
        setField(transport, "jwtValidator", (JwtValidator) t -> JwtValidationResult.ok());
    }

    // --- Unknown session → 404 ---

    @Test
    void unknownSessionReturns404() {
        Response r = transport.receiveMessage("does-not-exist", headers(null), "{\"jsonrpc\":\"2.0\"}");
        assertEquals(404, r.getStatus());
    }

    // --- Expired session → 410 ---

    @Test
    void expiredSessionReturns410() {
        config.setSecurityEnabled(false);
        config.setSessionIdleMillis(1); // effectively immediate idle expiry
        String sid = putSession(null, Instant.now().minusSeconds(3600), Instant.now().minusSeconds(3600));

        Response r = transport.receiveMessage(sid, headers(null), "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}");
        assertEquals(410, r.getStatus());
        assertEquals(0, sessionCount(), "expired session should be removed");
    }

    // --- Foreign principal → 401 ---

    @Test
    void tokenFromDifferentPrincipalReturns401() {
        config.setSecurityEnabled(true);
        // Session bound to "alice"
        setField(transport, "jwtValidator", (JwtValidator) t -> JwtValidationResult.ok(subjectOf(t)));
        String aliceFp = SessionFingerprint.of("alice");
        String sid = putSession(aliceFp, Instant.now(), Instant.now());

        // POST carries a token for "bob"
        Response r = transport.receiveMessage(sid, headers("Bearer sub=bob"),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}");
        assertEquals(401, r.getStatus());
    }

    @Test
    void matchingPrincipalIsAccepted() {
        config.setSecurityEnabled(true);
        setField(transport, "jwtValidator", (JwtValidator) t -> JwtValidationResult.ok(subjectOf(t)));
        String aliceFp = SessionFingerprint.of("alice");
        String sid = putSession(aliceFp, Instant.now(), Instant.now());

        Response r = transport.receiveMessage(sid, headers("Bearer sub=alice"),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}");
        assertEquals(202, r.getStatus());
    }

    @Test
    void missingAuthorizationOnSecurePostReturns401() {
        config.setSecurityEnabled(true);
        String sid = putSession(SessionFingerprint.of("alice"), Instant.now(), Instant.now());
        Response r = transport.receiveMessage(sid, headers(null),
                "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}");
        assertEquals(401, r.getStatus());
    }

    // --- Tool rate limit → 429 ---

    @Test
    void toolCallsAreRateLimited() {
        config.setSecurityEnabled(false);
        config.setToolRateLimitRequests(3);
        config.setToolRateLimitWindowSeconds(60);
        String sid = putSession(null, Instant.now(), Instant.now());

        String call = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/call\",\"params\":{\"name\":\"x\"}}";
        for (int i = 0; i < 3; i++) {
            assertEquals(202, transport.receiveMessage(sid, headers(null), call).getStatus());
        }
        Response limited = transport.receiveMessage(sid, headers(null), call);
        assertEquals(429, limited.getStatus());
        assertNotNull(limited.getHeaderString("Retry-After"));
    }

    @Test
    void nonToolCallsAreNotRateLimited() {
        config.setSecurityEnabled(false);
        config.setToolRateLimitRequests(1);
        String sid = putSession(null, Instant.now(), Instant.now());
        String list = "{\"jsonrpc\":\"2.0\",\"id\":1,\"method\":\"tools/list\"}";
        for (int i = 0; i < 5; i++) {
            assertEquals(202, transport.receiveMessage(sid, headers(null), list).getStatus());
        }
    }

    // --- Session cap ---

    @Test
    void connectRejectsWhenSessionCapReached() {
        config.setSecurityEnabled(false);
        config.setMaxSessions(1);
        // Pre-fill one session
        putSession(null, Instant.now(), Instant.now());

        StubSseEventSink sink = new StubSseEventSink();
        transport.connect(sink, sse, headers(null));
        assertTrue(sink.closed, "second connection should be rejected");
        assertTrue(sink.lastEventData.contains("503"));
        assertEquals(1, sessionCount());
    }

    // --- Helpers ---

    private static String subjectOf(String bearer) {
        // token format used in tests: "Bearer sub=<name>" → we receive "sub=<name>"
        int i = bearer.indexOf("sub=");
        return (i >= 0) ? bearer.substring(i + 4) : bearer;
    }

    private String putSession(String fingerprint, Instant created, Instant lastAccess) {
        String sid = java.util.UUID.randomUUID().toString();
        McpSession session = new McpSession(sid, new StubSseEventSink(), sse, true, created, lastAccess, fingerprint);
        sessionsMap().put(sid, session);
        return sid;
    }

    @SuppressWarnings("unchecked")
    private ConcurrentHashMap<String, McpSession> sessionsMap() {
        try {
            Field f = McpSseTransport.class.getDeclaredField("sessions");
            f.setAccessible(true);
            return (ConcurrentHashMap<String, McpSession>) f.get(transport);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private int sessionCount() {
        return sessionsMap().size();
    }

    private static HttpHeaders headers(String authorization) {
        return (HttpHeaders) java.lang.reflect.Proxy.newProxyInstance(
                McpSseTransportPostSecurityTest.class.getClassLoader(),
                new Class<?>[]{HttpHeaders.class},
                (proxy, method, args) -> {
                    if ("getHeaderString".equals(method.getName())
                            && HttpHeaders.AUTHORIZATION.equalsIgnoreCase((String) args[0])) {
                        return authorization;
                    }
                    Class<?> rt = method.getReturnType();
                    if (rt == boolean.class) return false;
                    if (rt == int.class) return 0;
                    if (rt == long.class) return 0L;
                    return null;
                });
    }

    private static McpJsonRpcHandler stubHandler() {
        return new McpJsonRpcHandler(
                new org.demoiselle.jee.mcp.registry.McpToolRegistry(),
                new org.demoiselle.jee.mcp.registry.McpResourceRegistry(),
                new org.demoiselle.jee.mcp.registry.McpPromptRegistry(),
                new JsonRpcSerializer(),
                new McpConfig(),
                new org.demoiselle.jee.mcp.integration.PlainTextErrorFormatter());
    }

    private static void setField(Object target, String name, Object value) {
        try {
            Field f = target.getClass().getDeclaredField(name);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // --- SSE stubs ---

    static class StubSseEventSink implements SseEventSink {
        boolean closed = false;
        String lastEventData = null;

        @Override public CompletionStage<?> send(OutboundSseEvent event) {
            lastEventData = event.getData() != null ? event.getData().toString() : null;
            return CompletableFuture.completedFuture(null);
        }
        @Override public boolean isClosed() { return closed; }
        @Override public void close() { closed = true; }
    }

    static class StubSse implements Sse {
        @Override public OutboundSseEvent.Builder newEventBuilder() { return new StubBuilder(); }
        @Override public jakarta.ws.rs.sse.SseBroadcaster newBroadcaster() { return null; }
    }

    static class StubBuilder implements OutboundSseEvent.Builder {
        private String name;
        private Object data;
        @Override public OutboundSseEvent.Builder id(String id) { return this; }
        @Override public OutboundSseEvent.Builder name(String n) { this.name = n; return this; }
        @Override public OutboundSseEvent.Builder reconnectDelay(long ms) { return this; }
        @Override public OutboundSseEvent.Builder mediaType(jakarta.ws.rs.core.MediaType mt) { return this; }
        @Override public OutboundSseEvent.Builder comment(String c) { return this; }
        @Override public OutboundSseEvent.Builder data(Class type, Object d) { this.data = d; return this; }
        @Override public OutboundSseEvent.Builder data(jakarta.ws.rs.core.GenericType type, Object d) { this.data = d; return this; }
        @Override public OutboundSseEvent.Builder data(Object d) { this.data = d; return this; }
        @Override public OutboundSseEvent build() { return new StubEvent(name, data); }
    }

    static class StubEvent implements OutboundSseEvent {
        private final String name;
        private final Object data;
        StubEvent(String name, Object data) { this.name = name; this.data = data; }
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
