/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.jsonrpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;

/**
 * Serialização/desserialização de mensagens JSON-RPC 2.0 via Jackson.
 *
 * <p>Configurado para omitir campos {@code null} na serialização e tolerar
 * propriedades desconhecidas na desserialização.</p>
 */
@ApplicationScoped
public class JsonRpcSerializer {

    private final ObjectMapper mapper;

    public JsonRpcSerializer() {
        this.mapper = new ObjectMapper()
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    /**
     * Desserializa uma string JSON em uma {@link JsonRpcMessage}.
     *
     * @param json a string JSON representando uma mensagem JSON-RPC
     * @return a mensagem desserializada
     * @throws JsonRpcSerializationException se o JSON for inválido
     */
    public JsonRpcMessage deserialize(String json) {
        try {
            return mapper.readValue(json, JsonRpcMessage.class);
        } catch (JsonProcessingException e) {
            throw new JsonRpcSerializationException("Failed to deserialize JSON-RPC message", e);
        }
    }

    /**
     * Serializa uma {@link JsonRpcMessage} em uma string JSON.
     *
     * @param message a mensagem JSON-RPC a ser serializada
     * @return a string JSON resultante
     * @throws JsonRpcSerializationException se a serialização falhar
     */
    public String serialize(JsonRpcMessage message) {
        try {
            return mapper.writeValueAsString(message);
        } catch (JsonProcessingException e) {
            throw new JsonRpcSerializationException("Failed to serialize JSON-RPC message", e);
        }
    }
}
