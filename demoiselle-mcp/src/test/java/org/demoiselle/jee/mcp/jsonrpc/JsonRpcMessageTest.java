/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.jsonrpc;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link JsonRpcMessage} and {@link JsonRpcError}.
 */
class JsonRpcMessageTest {

    // -----------------------------------------------------------------------
    // JsonRpcError constants
    // -----------------------------------------------------------------------

    @Test
    void errorConstantsHaveCorrectValues() {
        assertEquals(-32700, JsonRpcError.PARSE_ERROR);
        assertEquals(-32600, JsonRpcError.INVALID_REQUEST);
        assertEquals(-32601, JsonRpcError.METHOD_NOT_FOUND);
        assertEquals(-32602, JsonRpcError.INVALID_PARAMS);
        assertEquals(-32603, JsonRpcError.INTERNAL_ERROR);
    }

    @Test
    void errorRecordFieldsAccessible() {
        JsonRpcError error = new JsonRpcError(-32600, "Invalid Request", "extra");
        assertEquals(-32600, error.code());
        assertEquals("Invalid Request", error.message());
        assertEquals("extra", error.data());
    }

    @Test
    void errorRecordWithNullData() {
        JsonRpcError error = new JsonRpcError(-32700, "Parse error", null);
        assertNull(error.data());
    }

    // -----------------------------------------------------------------------
    // JsonRpcMessage.success()
    // -----------------------------------------------------------------------

    @Test
    void successCreatesCorrectMessage() {
        JsonRpcMessage msg = JsonRpcMessage.success(1, "hello");
        assertEquals("2.0", msg.jsonrpc());
        assertNull(msg.method());
        assertNull(msg.params());
        assertEquals(1, msg.id());
        assertEquals("hello", msg.result());
        assertNull(msg.error());
    }

    // -----------------------------------------------------------------------
    // JsonRpcMessage.error()
    // -----------------------------------------------------------------------

    @Test
    void errorCreatesCorrectMessage() {
        JsonRpcError err = new JsonRpcError(JsonRpcError.INTERNAL_ERROR, "boom", null);
        JsonRpcMessage msg = JsonRpcMessage.error(42, err);
        assertEquals("2.0", msg.jsonrpc());
        assertNull(msg.method());
        assertNull(msg.params());
        assertEquals(42, msg.id());
        assertNull(msg.result());
        assertSame(err, msg.error());
    }

    // -----------------------------------------------------------------------
    // JsonRpcMessage.isNotification()
    // -----------------------------------------------------------------------

    @Test
    void isNotificationReturnsTrueWhenIdIsNull() {
        JsonRpcMessage msg = new JsonRpcMessage("2.0", "initialized", null, null, null, null);
        assertTrue(msg.isNotification());
    }

    @Test
    void isNotificationReturnsFalseWhenIdIsPresent() {
        JsonRpcMessage msg = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
        assertFalse(msg.isNotification());
    }

    @Test
    void isNotificationReturnsFalseWhenIdIsStringId() {
        JsonRpcMessage msg = new JsonRpcMessage("2.0", "tools/list", null, "abc-123", null, null);
        assertFalse(msg.isNotification());
    }
}
