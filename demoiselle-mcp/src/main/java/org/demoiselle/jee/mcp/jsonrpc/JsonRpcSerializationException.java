/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.jsonrpc;

/**
 * Exceção lançada quando a serialização ou desserialização de uma mensagem JSON-RPC falha.
 */
public class JsonRpcSerializationException extends RuntimeException {

    public JsonRpcSerializationException(String message, Throwable cause) {
        super(message, cause);
    }
}
