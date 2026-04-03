/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.jsonrpc;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonRpcSerializer}.
 */
class JsonRpcSerializerTest {

    private JsonRpcSerializer serializer;

    @BeforeEach
    void setUp() {
        serializer = new JsonRpcSerializer();
    }

    // -----------------------------------------------------------------------
    // Serialization
    // -----------------------------------------------------------------------

    @Test
    void serializeSuccessResponseOmitsNullFields() {
        JsonRpcMessage msg = JsonRpcMessage.success(1, "ok");
        String json = serializer.serialize(msg);

        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"id\":1"));
        assertTrue(json.contains("\"result\":\"ok\""));
        // null fields must be omitted
        assertFalse(json.contains("\"method\""));
        assertFalse(json.contains("\"params\""));
        assertFalse(json.contains("\"error\""));
    }

    @Test
    void serializeErrorResponseOmitsNullFields() {
        JsonRpcError err = new JsonRpcError(JsonRpcError.PARSE_ERROR, "Parse error", null);
        JsonRpcMessage msg = JsonRpcMessage.error(null, err);
        String json = serializer.serialize(msg);

        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"error\""));
        assertTrue(json.contains("-32700"));
        // null fields must be omitted
        assertFalse(json.contains("\"method\""));
        assertFalse(json.contains("\"params\""));
        assertFalse(json.contains("\"result\""));
        assertFalse(json.contains("\"id\""));
        // data is null inside error, should be omitted too
        assertFalse(json.contains("\"data\""));
    }

    @Test
    void serializeRequestMessage() {
        JsonRpcMessage msg = new JsonRpcMessage("2.0", "tools/list", null, 5, null, null);
        String json = serializer.serialize(msg);

        assertTrue(json.contains("\"jsonrpc\":\"2.0\""));
        assertTrue(json.contains("\"method\":\"tools/list\""));
        assertTrue(json.contains("\"id\":5"));
        assertFalse(json.contains("\"params\""));
        assertFalse(json.contains("\"result\""));
        assertFalse(json.contains("\"error\""));
    }

    @Test
    void serializeDoesNotIncludeIsNotificationField() {
        JsonRpcMessage msg = new JsonRpcMessage("2.0", "initialized", null, null, null, null);
        String json = serializer.serialize(msg);
        assertFalse(json.contains("isNotification"), "isNotification() must not be serialized as a JSON property");
        assertFalse(json.contains("notification"), "notification must not appear in serialized JSON");
    }

    // -----------------------------------------------------------------------
    // Deserialization
    // -----------------------------------------------------------------------

    @Test
    void deserializeSuccessResponse() {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"hello\"}";
        JsonRpcMessage msg = serializer.deserialize(json);

        assertEquals("2.0", msg.jsonrpc());
        assertEquals(1, msg.id());
        assertEquals("hello", msg.result());
        assertNull(msg.method());
        assertNull(msg.params());
        assertNull(msg.error());
    }

    @Test
    void deserializeErrorResponse() {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":2,\"error\":{\"code\":-32601,\"message\":\"Method not found\"}}";
        JsonRpcMessage msg = serializer.deserialize(json);

        assertEquals("2.0", msg.jsonrpc());
        assertEquals(2, msg.id());
        assertNull(msg.result());
        assertNotNull(msg.error());
        assertEquals(JsonRpcError.METHOD_NOT_FOUND, msg.error().code());
        assertEquals("Method not found", msg.error().message());
        assertNull(msg.error().data());
    }

    @Test
    void deserializeRequestMessage() {
        String json = "{\"jsonrpc\":\"2.0\",\"method\":\"tools/call\",\"params\":{\"name\":\"calc\"},\"id\":3}";
        JsonRpcMessage msg = serializer.deserialize(json);

        assertEquals("2.0", msg.jsonrpc());
        assertEquals("tools/call", msg.method());
        assertNotNull(msg.params());
        assertEquals(3, msg.id());
        assertNull(msg.result());
        assertNull(msg.error());
    }

    @Test
    void deserializeNotification() {
        String json = "{\"jsonrpc\":\"2.0\",\"method\":\"initialized\"}";
        JsonRpcMessage msg = serializer.deserialize(json);

        assertEquals("2.0", msg.jsonrpc());
        assertEquals("initialized", msg.method());
        assertNull(msg.id());
        assertTrue(msg.isNotification());
    }

    @Test
    void deserializeToleratesUnknownProperties() {
        String json = "{\"jsonrpc\":\"2.0\",\"id\":1,\"result\":\"ok\",\"extraField\":\"ignored\"}";
        JsonRpcMessage msg = serializer.deserialize(json);

        assertEquals("2.0", msg.jsonrpc());
        assertEquals(1, msg.id());
        assertEquals("ok", msg.result());
    }

    @Test
    void deserializeInvalidJsonThrowsException() {
        assertThrows(JsonRpcSerializationException.class,
                () -> serializer.deserialize("not valid json"));
    }

    // -----------------------------------------------------------------------
    // Round-trip
    // -----------------------------------------------------------------------

    @Test
    void roundTripSuccessMessage() {
        JsonRpcMessage original = JsonRpcMessage.success(10, "result-value");
        String json = serializer.serialize(original);
        JsonRpcMessage restored = serializer.deserialize(json);

        assertEquals(original.jsonrpc(), restored.jsonrpc());
        assertEquals(original.id(), restored.id());
        assertEquals(original.result(), restored.result());
        assertNull(restored.method());
        assertNull(restored.error());
    }

    @Test
    void roundTripErrorMessage() {
        JsonRpcError err = new JsonRpcError(JsonRpcError.INTERNAL_ERROR, "Internal error", "details");
        JsonRpcMessage original = JsonRpcMessage.error(7, err);
        String json = serializer.serialize(original);
        JsonRpcMessage restored = serializer.deserialize(json);

        assertEquals(original.jsonrpc(), restored.jsonrpc());
        assertEquals(original.id(), restored.id());
        assertNull(restored.result());
        assertNotNull(restored.error());
        assertEquals(err.code(), restored.error().code());
        assertEquals(err.message(), restored.error().message());
        assertEquals(err.data(), restored.error().data());
    }
}
