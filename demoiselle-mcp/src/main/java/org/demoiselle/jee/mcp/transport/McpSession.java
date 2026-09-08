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
 * <p>Mantém referência ao {@link SseEventSink} e {@link Sse} associados a uma
 * conexão SSE de um cliente MCP, junto com metadados da sessão. A sessão é
 * vinculada a uma <em>impressão digital</em> SHA-256 não reversível do
 * token/principal que a estabeleceu ({@code principalFingerprint}); o token
 * bruto nunca é armazenado.</p>
 *
 * @param id                   identificador único da sessão (UUID)
 * @param sink                 o sink SSE para envio de eventos ao cliente
 * @param sse                  a instância SSE para criação de eventos
 * @param initialized          indica se o handshake initialize/initialized foi concluído
 * @param createdAt            instante de criação da sessão
 * @param lastAccessAt         instante do último acesso (para expiração por inatividade)
 * @param principalFingerprint SHA-256 hex do principal/token vinculado, ou {@code null}
 *                             quando a segurança está desativada
 */
record McpSession(
    String id,
    SseEventSink sink,
    Sse sse,
    boolean initialized,
    Instant createdAt,
    Instant lastAccessAt,
    String principalFingerprint
) {

    /**
     * Returns a copy with {@code lastAccessAt} refreshed to {@code now}.
     *
     * @param now the new last-access instant
     * @return the refreshed session
     */
    McpSession touch(Instant now) {
        return new McpSession(id, sink, sse, initialized, createdAt, now, principalFingerprint);
    }

    /**
     * Whether the session has exceeded its allowed lifetime or idle window.
     *
     * @param now          the reference instant
     * @param ttlMillis    absolute lifetime cap (from {@code createdAt}); {@code <= 0} disables
     * @param idleMillis   idle timeout (from {@code lastAccessAt}); {@code <= 0} disables
     * @return {@code true} when expired
     */
    boolean isExpired(Instant now, long ttlMillis, long idleMillis) {
        if (ttlMillis > 0 && createdAt != null
                && now.toEpochMilli() - createdAt.toEpochMilli() >= ttlMillis) {
            return true;
        }
        return idleMillis > 0 && lastAccessAt != null
                && now.toEpochMilli() - lastAccessAt.toEpochMilli() >= idleMillis;
    }
}
