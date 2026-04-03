/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.jsonrpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Mensagem JSON-RPC 2.0 genérica.
 *
 * <p>Representa tanto requisições (com {@code method} e {@code params}) quanto
 * respostas (com {@code result} ou {@code error}) do protocolo JSON-RPC 2.0
 * utilizado pelo MCP.</p>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record JsonRpcMessage(
    String jsonrpc,
    String method,
    Object params,
    Object id,
    Object result,
    JsonRpcError error
) {

    /**
     * Cria uma resposta de sucesso JSON-RPC 2.0.
     *
     * @param id     o identificador da requisição original
     * @param result o resultado da operação
     * @return mensagem JSON-RPC de sucesso
     */
    public static JsonRpcMessage success(Object id, Object result) {
        return new JsonRpcMessage("2.0", null, null, id, result, null);
    }

    /**
     * Cria uma resposta de erro JSON-RPC 2.0.
     *
     * @param id    o identificador da requisição original
     * @param error o objeto de erro JSON-RPC
     * @return mensagem JSON-RPC de erro
     */
    public static JsonRpcMessage error(Object id, JsonRpcError error) {
        return new JsonRpcMessage("2.0", null, null, id, null, error);
    }

    /**
     * Verifica se esta mensagem é uma notificação (sem campo {@code id}).
     *
     * @return {@code true} se a mensagem não possui {@code id}
     */
    @JsonIgnore
    public boolean isNotification() {
        return id == null;
    }
}
