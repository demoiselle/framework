/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.handler.McpJsonRpcHandler;
import org.demoiselle.jee.mcp.integration.JwtValidationResult;
import org.demoiselle.jee.mcp.integration.JwtValidator;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcMessage;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Transporte SSE (Server-Sent Events) via JAX-RS para o protocolo MCP.
 *
 * <p>Endpoints:</p>
 * <ul>
 *   <li>{@code GET /mcp/sse} — estabelece a conexão SSE. Quando a segurança
 *       está ativa, exige um JWT bearer válido; a sessão é vinculada a uma
 *       impressão digital SHA-256 não reversível do principal/token.</li>
 *   <li>{@code POST /mcp/messages?sessionId=...} — recebe mensagens JSON-RPC.
 *       Com segurança ativa, revalida o {@code Authorization} e rejeita
 *       (401) um token de outro principal; sessões inexistentes/expiradas
 *       resultam em 404/410; excesso de chamadas de ferramentas resulta em
 *       429.</li>
 * </ul>
 *
 * <p>Regras de segurança e robustez:</p>
 * <ul>
 *   <li>o {@code sessionId} nunca é registrado em nível INFO (apenas FINE);</li>
 *   <li>TTL absoluto e timeout por inatividade das sessões;</li>
 *   <li>teto de sessões concorrentes;</li>
 *   <li>rate limit real por sessão nas invocações de ferramentas.</li>
 * </ul>
 */
@Path("/mcp")
@ApplicationScoped
public class McpSseTransport {

    private static final Logger LOGGER = Logger.getLogger(McpSseTransport.class.getName());
    private static final String BEARER_PREFIX = "Bearer ";

    private final ConcurrentHashMap<String, McpSession> sessions = new ConcurrentHashMap<>();

    private McpToolRateLimiter toolRateLimiter;

    @Inject
    McpJsonRpcHandler handler;

    @Inject
    JsonRpcSerializer serializer;

    @Inject
    McpConfig config;

    @Inject
    JwtValidator jwtValidator;

    @PostConstruct
    void init() {
        int max = (config != null) ? config.getMaxSessions() : 10_000;
        this.toolRateLimiter = new McpToolRateLimiter(max);
    }

    private McpToolRateLimiter rateLimiter() {
        if (toolRateLimiter == null) {
            int max = (config != null) ? config.getMaxSessions() : 10_000;
            toolRateLimiter = new McpToolRateLimiter(max);
        }
        return toolRateLimiter;
    }

    // ── GET /mcp/sse ────────────────────────────────────────────────────

    /**
     * Estabelece uma conexão SSE com o cliente.
     *
     * @param sink    o sink SSE fornecido pelo container JAX-RS
     * @param sse     a instância SSE para criação de eventos
     * @param headers os headers HTTP da requisição
     */
    @GET
    @Path("/sse")
    @Produces(MediaType.SERVER_SENT_EVENTS)
    public void connect(@Context SseEventSink sink, @Context Sse sse,
                        @Context HttpHeaders headers) {

        String fingerprint = null;

        if (config.isSecurityEnabled()) {
            String token = bearerToken(headers);
            if (token == null) {
                LOGGER.log(Level.WARNING, "SSE connection rejected: missing or malformed Authorization header");
                sendUnauthorizedAndClose(sink, sse, "Missing or invalid JWT token");
                return;
            }

            JwtValidationResult result = jwtValidator.validate(token);
            if (!result.valid()) {
                String detail = result.expired() ? "Token expired" : "Invalid JWT token";
                LOGGER.log(Level.WARNING, "SSE connection rejected: {0}", detail);
                sendUnauthorizedAndClose(sink, sse, detail);
                return;
            }
            // Bind the session to a non-reversible fingerprint of the principal
            // (falling back to the token when no subject is exposed).
            String principal = (result.subject() != null && !result.subject().isBlank())
                    ? result.subject() : token;
            fingerprint = SessionFingerprint.of(principal);
        }

        // Enforce concurrent-session cap (after purging expired ones).
        purgeExpired();
        if (sessions.size() >= config.getMaxSessions()) {
            LOGGER.log(Level.WARNING, "SSE connection rejected: session cap reached ({0})",
                    config.getMaxSessions());
            sendErrorAndClose(sink, sse, 503, "Service Unavailable", "Too many active sessions");
            return;
        }

        String sessionId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        McpSession session = new McpSession(sessionId, sink, sse, false, now, now, fingerprint);
        sessions.put(sessionId, session);

        LOGGER.log(Level.FINE, "SSE connection established: sessionId={0}", sessionId);

        String endpointUri = "/mcp/messages?sessionId=" + sessionId;
        final String sid = sessionId;
        sink.send(sse.newEventBuilder()
                .name("endpoint")
                .data(endpointUri)
                .build())
            .whenComplete((v, error) -> {
                if (error != null) {
                    sessions.remove(sid);
                    LOGGER.log(Level.FINE, "SSE connection failed during endpoint event: sessionId={0}", sid);
                }
            });
    }

    // ── POST /mcp/messages ──────────────────────────────────────────────

    /**
     * Recebe mensagens JSON-RPC do cliente via POST.
     *
     * @param sessionId o identificador da sessão SSE
     * @param headers   os headers HTTP (usados para revalidar o Authorization)
     * @param body      o corpo da requisição contendo a mensagem JSON-RPC
     * @return 202 quando aceito; 401 principal divergente; 404/410 sessão
     *         inexistente/expirada; 429 quando o rate limit é excedido
     */
    @POST
    @Path("/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveMessage(@QueryParam("sessionId") String sessionId,
                                   @Context HttpHeaders headers,
                                   String body) {
        McpSession session = sessions.get(sessionId);
        if (session == null) {
            LOGGER.log(Level.FINE, "Rejected POST for unknown sessionId={0}", sessionId);
            return problem(Response.Status.NOT_FOUND, 404, "Not Found",
                    "Invalid or expired session ID");
        }

        // Expiry (TTL / idle) → 410 Gone.
        Instant now = Instant.now();
        if (session.isExpired(now, config.getSessionTtlMillis(), config.getSessionIdleMillis())) {
            removeSession(sessionId);
            LOGGER.log(Level.FINE, "Rejected POST for expired sessionId={0}", sessionId);
            return problem(Response.Status.GONE, 410, "Gone", "Session expired");
        }

        // Re-authenticate and enforce principal binding when security is active.
        if (config.isSecurityEnabled()) {
            String token = bearerToken(headers);
            if (token == null) {
                return problem(Response.Status.UNAUTHORIZED, 401, "Unauthorized",
                        "Missing or invalid JWT token");
            }
            JwtValidationResult result = jwtValidator.validate(token);
            if (!result.valid()) {
                String detail = result.expired() ? "Token expired" : "Invalid JWT token";
                return problem(Response.Status.UNAUTHORIZED, 401, "Unauthorized", detail);
            }
            String principal = (result.subject() != null && !result.subject().isBlank())
                    ? result.subject() : token;
            String fingerprint = SessionFingerprint.of(principal);
            if (session.principalFingerprint() != null
                    && !session.principalFingerprint().equals(fingerprint)) {
                LOGGER.log(Level.WARNING, "Rejected POST: token principal does not match session binding");
                return problem(Response.Status.UNAUTHORIZED, 401, "Unauthorized",
                        "Token does not match session principal");
            }
        }

        // Real per-session tool rate limiting for tools/call.
        if (isToolCall(body)) {
            int retryAfter = rateLimiter().recordAndCheck(sessionId,
                    config.getToolRateLimitRequests(), config.getToolRateLimitWindowSeconds());
            if (retryAfter > 0) {
                LOGGER.log(Level.FINE, "Tool rate limit exceeded for sessionId={0}", sessionId);
                return Response.status(429)
                        .header("Retry-After", retryAfter)
                        .entity("{\"type\":\"about:blank\",\"title\":\"Too Many Requests\","
                                + "\"status\":429,\"detail\":\"Tool rate limit exceeded. Retry after "
                                + retryAfter + " seconds\"}")
                        .type(MediaType.APPLICATION_JSON)
                        .build();
            }
        }

        // Refresh idle timer.
        sessions.computeIfPresent(sessionId, (id, s) -> s.touch(now));

        try {
            JsonRpcMessage request = serializer.deserialize(body);
            JsonRpcMessage response = handler.handle(sessionId, request);

            if (response != null) {
                sendMessage(sessionId, session, serializer.serialize(response));
            }
            return Response.accepted().build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing message", e);
            try {
                if (!session.sink().isClosed()) {
                    JsonRpcMessage errorResponse = JsonRpcMessage.error(null,
                            new org.demoiselle.jee.mcp.jsonrpc.JsonRpcError(
                                    org.demoiselle.jee.mcp.jsonrpc.JsonRpcError.PARSE_ERROR,
                                    "Parse error: " + e.getMessage(), null));
                    sendMessage(sessionId, session, serializer.serialize(errorResponse));
                }
            } catch (Exception inner) {
                LOGGER.log(Level.SEVERE, "Failed to send error response via SSE", inner);
            }
            return Response.accepted().build();
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────

    private void sendMessage(String sessionId, McpSession session, String json) {
        SseEventSink sink = session.sink();
        Sse sse = session.sse();
        if (!sink.isClosed()) {
            sink.send(sse.newEventBuilder().name("message").data(json).build());
        } else {
            removeSession(sessionId);
            LOGGER.log(Level.FINE, "SSE sink closed for sessionId={0}, removing session", sessionId);
        }
    }

    private void removeSession(String sessionId) {
        sessions.remove(sessionId);
        rateLimiter().remove(sessionId);
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        long ttl = config.getSessionTtlMillis();
        long idle = config.getSessionIdleMillis();
        sessions.entrySet().removeIf(e -> {
            boolean expired = e.getValue().isExpired(now, ttl, idle);
            if (expired) {
                rateLimiter().remove(e.getKey());
            }
            return expired;
        });
    }

    private static String bearerToken(HttpHeaders headers) {
        if (headers == null) {
            return null;
        }
        String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);
        if (authHeader == null
                || !authHeader.regionMatches(true, 0, BEARER_PREFIX, 0, BEARER_PREFIX.length())) {
            return null;
        }
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        return token.isEmpty() ? null : token;
    }

    private boolean isToolCall(String body) {
        if (body == null || body.isBlank()) {
            return false;
        }
        try {
            JsonRpcMessage msg = serializer.deserialize(body);
            return "tools/call".equals(msg.method());
        } catch (Exception e) {
            return false;
        }
    }

    private Response problem(Response.Status status, int code, String title, String detail) {
        return Response.status(status)
                .entity("{\"type\":\"about:blank\",\"title\":\"" + title + "\","
                        + "\"status\":" + code + ",\"detail\":\"" + detail + "\"}")
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private void sendUnauthorizedAndClose(SseEventSink sink, Sse sse, String detail) {
        sendErrorAndClose(sink, sse, 401, "Unauthorized", detail);
    }

    private void sendErrorAndClose(SseEventSink sink, Sse sse, int code, String title, String detail) {
        try {
            String problemDetail = "{\"type\":\"about:blank\",\"title\":\"" + title + "\","
                    + "\"status\":" + code + ",\"detail\":\"" + detail + "\"}";
            sink.send(sse.newEventBuilder().name("error").data(problemDetail).build());
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to send error event", e);
        } finally {
            try {
                sink.close();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Failed to close SSE sink after error", e);
            }
        }
    }
}
