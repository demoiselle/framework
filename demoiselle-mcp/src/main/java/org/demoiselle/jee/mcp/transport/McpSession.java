/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import jakarta.ws.rs.sse.Sse;
import jakarta.ws.rs.sse.SseEventSink;

import java.time.Instant;

/**
 * Sessão SSE ativa.
 *
 * <p>Mantém referência ao {@link SseEventSink} e {@link Sse} associados
 * a uma conexão SSE de um cliente MCP, junto com metadados da sessão.</p>
 *
 * @param id          identificador único da sessão (UUID)
 * @param sink        o sink SSE para envio de eventos ao cliente
 * @param sse         a instância SSE para criação de eventos
 * @param initialized indica se o handshake initialize/initialized foi concluído
 * @param createdAt   instante de criação da sessão
 */
record McpSession(
    String id,
    SseEventSink sink,
    Sse sse,
    boolean initialized,
    Instant createdAt
) {}
