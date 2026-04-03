/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

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
 * <p>Expõe dois endpoints:</p>
 * <ul>
 *   <li>{@code GET /mcp/sse} — estabelece conexão SSE e envia evento {@code endpoint}
 *       com a URI do POST para envio de mensagens</li>
 *   <li>{@code POST /mcp/messages?sessionId=...} — recebe mensagens JSON-RPC do cliente,
 *       delega ao {@link McpJsonRpcHandler} e envia a resposta como evento SSE</li>
 * </ul>
 */
@Path("/mcp")
@ApplicationScoped
public class McpSseTransport {

    private static final Logger LOGGER = Logger.getLogger(McpSseTransport.class.getName());

    private final ConcurrentHashMap<String, McpSession> sessions = new ConcurrentHashMap<>();

    @Inject
    McpJsonRpcHandler handler;

    @Inject
    JsonRpcSerializer serializer;

    @Inject
    McpConfig config;

    @Inject
    JwtValidator jwtValidator;

    private static final String BEARER_PREFIX = "Bearer ";

    /**
     * Estabelece uma conexão SSE com o cliente.
     *
     * <p>When {@code demoiselle.mcp.security.enabled} is {@code true}, the
     * {@code Authorization} header must contain a valid JWT bearer token.
     * Missing, invalid or expired tokens result in HTTP 401.</p>
     *
     * <p>Gera um {@code sessionId} único (UUID), registra a sessão e envia
     * um evento {@code endpoint} contendo a URI do endpoint POST para envio
     * de mensagens JSON-RPC.</p>
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

        // JWT authentication when security is enabled
        if (config.isSecurityEnabled()) {
            String authHeader = headers.getHeaderString(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                LOGGER.log(Level.WARNING, "SSE connection rejected: missing or malformed Authorization header");
                sendUnauthorizedAndClose(sink, sse, "Missing or invalid JWT token");
                return;
            }

            String token = authHeader.substring(BEARER_PREFIX.length()).trim();
            if (token.isEmpty()) {
                LOGGER.log(Level.WARNING, "SSE connection rejected: empty bearer token");
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
        }

        String sessionId = UUID.randomUUID().toString();
        McpSession session = new McpSession(sessionId, sink, sse, false, Instant.now());
        sessions.put(sessionId, session);

        LOGGER.log(Level.FINE, "SSE connection established: sessionId={0}", sessionId);

        // Send the endpoint event with the POST URI for this session
        String endpointUri = "/mcp/messages?sessionId=" + sessionId;
        sink.send(sse.newEventBuilder()
                .name("endpoint")
                .data(endpointUri)
                .build())
            .whenComplete((v, error) -> {
                if (error != null) {
                    // Client disconnected before receiving the endpoint event
                    sessions.remove(sessionId);
                    LOGGER.log(Level.FINE, "SSE connection failed during endpoint event: sessionId={0}", sessionId);
                }
            });
    }

    /**
     * Sends an HTTP 401 ProblemDetail-style error as an SSE event and closes the sink.
     */
    private void sendUnauthorizedAndClose(SseEventSink sink, Sse sse, String detail) {
        try {
            String problemDetail = "{\"type\":\"about:blank\",\"title\":\"Unauthorized\","
                    + "\"status\":401,\"detail\":\"" + detail + "\"}";
            sink.send(sse.newEventBuilder()
                    .name("error")
                    .data(problemDetail)
                    .build());
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Failed to send 401 error event", e);
        } finally {
            try {
                sink.close();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Failed to close SSE sink after auth failure", e);
            }
        }
    }

    /**
     * Recebe mensagens JSON-RPC do cliente via POST.
     *
     * <p>Valida o {@code sessionId}, desserializa a mensagem JSON-RPC,
     * delega ao {@link McpJsonRpcHandler} e envia a resposta como evento
     * SSE na conexão correspondente.</p>
     *
     * @param sessionId o identificador da sessão SSE
     * @param body      o corpo da requisição contendo a mensagem JSON-RPC
     * @return HTTP 202 Accepted se processado com sucesso, HTTP 404 se sessionId inválido
     */
    @POST
    @Path("/messages")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response receiveMessage(@QueryParam("sessionId") String sessionId, String body) {
        McpSession session = sessions.get(sessionId);
        if (session == null) {
            LOGGER.log(Level.WARNING, "Invalid sessionId: {0}", sessionId);
            return Response.status(Response.Status.NOT_FOUND)
                    .entity("{\"type\":\"about:blank\",\"title\":\"Not Found\","
                            + "\"status\":404,\"detail\":\"Invalid or expired session ID\"}")
                    .type(MediaType.APPLICATION_JSON)
                    .build();
        }

        try {
            JsonRpcMessage request = serializer.deserialize(body);
            JsonRpcMessage response = handler.handle(sessionId, request);

            // Notifications produce no response
            if (response != null) {
                String responseJson = serializer.serialize(response);
                SseEventSink sink = session.sink();
                Sse sse = session.sse();

                if (!sink.isClosed()) {
                    sink.send(sse.newEventBuilder()
                            .name("message")
                            .data(responseJson)
                            .build());
                } else {
                    sessions.remove(sessionId);
                    LOGGER.log(Level.WARNING, "SSE sink closed for sessionId={0}, removing session", sessionId);
                }
            }

            return Response.accepted().build();

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Error processing message for sessionId=" + sessionId, e);
            // Send error response via SSE if possible
            try {
                SseEventSink sink = session.sink();
                Sse sse = session.sse();
                if (!sink.isClosed()) {
                    JsonRpcMessage errorResponse = JsonRpcMessage.error(null,
                            new org.demoiselle.jee.mcp.jsonrpc.JsonRpcError(
                                    org.demoiselle.jee.mcp.jsonrpc.JsonRpcError.PARSE_ERROR,
                                    "Parse error: " + e.getMessage(), null));
                    sink.send(sse.newEventBuilder()
                            .name("message")
                            .data(serializer.serialize(errorResponse))
                            .build());
                }
            } catch (Exception inner) {
                LOGGER.log(Level.SEVERE, "Failed to send error response via SSE", inner);
            }
            return Response.accepted().build();
        }
    }
}
