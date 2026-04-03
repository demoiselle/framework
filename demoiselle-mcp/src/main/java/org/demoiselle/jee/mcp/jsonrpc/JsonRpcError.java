/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.jsonrpc;

/**
 * Erro JSON-RPC 2.0.
 *
 * <p>Contém código de erro, mensagem descritiva e dados adicionais opcionais.
 * Define constantes para os códigos de erro padrão do JSON-RPC 2.0.</p>
 */
public record JsonRpcError(
    int code,
    String message,
    Object data
) {
    /** Parse error — invalid JSON was received. */
    public static final int PARSE_ERROR = -32700;

    /** Invalid Request — the JSON sent is not a valid Request object. */
    public static final int INVALID_REQUEST = -32600;

    /** Method not found — the method does not exist or is not available. */
    public static final int METHOD_NOT_FOUND = -32601;

    /** Invalid params — invalid method parameter(s). */
    public static final int INVALID_PARAMS = -32602;

    /** Internal error — internal JSON-RPC error. */
    public static final int INTERNAL_ERROR = -32603;
}
