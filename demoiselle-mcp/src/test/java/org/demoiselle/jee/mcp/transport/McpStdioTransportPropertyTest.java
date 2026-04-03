/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.transport;

import net.jqwik.api.*;

import org.demoiselle.jee.mcp.jsonrpc.JsonRpcError;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcMessage;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for the stdio transport line-delimited protocol.
 *
 * <p>Covers Property 9: Round-trip do transporte stdio — for every valid
 * JSON-RPC message, writing it as a newline-delimited line and reading it
 * back must produce an equivalent message.</p>
 */
class McpStdioTransportPropertyTest {

    private final JsonRpcSerializer serializer = new JsonRpcSerializer();

    // -----------------------------------------------------------------------
    // Arbitraries
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<Object> jsonRpcIds() {
        Arbitrary<Object> intIds = Arbitraries.integers().between(1, 100_000).map(i -> (Object) i);
        Arbitrary<Object> stringIds = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).map(s -> (Object) s);
        return Arbitraries.oneOf(intIds, stringIds);
    }

    @Provide
    Arbitrary<Object> simpleResults() {
        Arbitrary<Object> strings = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30).map(s -> (Object) s);
        Arbitrary<Object> ints = Arbitraries.integers().between(-10_000, 10_000).map(i -> (Object) i);
        return Arbitraries.oneOf(strings, ints);
    }

    @Provide
    Arbitrary<JsonRpcError> jsonRpcErrors() {
        Arbitrary<Integer> codes = Arbitraries.of(
                JsonRpcError.PARSE_ERROR,
                JsonRpcError.INVALID_REQUEST,
                JsonRpcError.METHOD_NOT_FOUND,
                JsonRpcError.INVALID_PARAMS,
                JsonRpcError.INTERNAL_ERROR
        );
        Arbitrary<String> messages = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30);
        return Combinators.combine(codes, messages).as((c, m) -> new JsonRpcError(c, m, null));
    }

    @Provide
    Arbitrary<JsonRpcMessage> validMessages() {
        Arbitrary<JsonRpcMessage> successMessages = Combinators.combine(
                jsonRpcIds(), simpleResults()
        ).as(JsonRpcMessage::success);

        Arbitrary<JsonRpcMessage> errorMessages = Combinators.combine(
                jsonRpcIds(), jsonRpcErrors()
        ).as(JsonRpcMessage::error);

        Arbitrary<JsonRpcMessage> requestMessages = Combinators.combine(
                Arbitraries.of("tools/list", "tools/call", "resources/list",
                        "resources/read", "prompts/list", "prompts/get", "initialize"),
                jsonRpcIds()
        ).as((method, id) -> new JsonRpcMessage("2.0", method, null, id, null, null));

        return Arbitraries.oneOf(successMessages, errorMessages, requestMessages);
    }

    // -----------------------------------------------------------------------
    // Property 9: Round-trip do transporte stdio
    // -----------------------------------------------------------------------

    /**
     * For every valid JSON-RPC message, serializing it to a JSON string,
     * writing it as a newline-delimited line, reading it back, and
     * deserializing must produce an equivalent message.
     *
     * <p>This tests the line-delimited protocol used by the stdio transport,
     * using ByteArrayOutputStream/ByteArrayInputStream to simulate the pipe.</p>
     *
     * <p><b>Validates: Requirements 5.1, 5.2</b></p>
     */
    @Property(tries = 100)
    void stdioRoundTripProducesEquivalentMessage(
            @ForAll("validMessages") JsonRpcMessage original) throws Exception {

        // 1. Serialize the message to JSON
        String json = serializer.serialize(original);

        // 2. Write as a newline-delimited line (simulating stdout)
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(baos, true, StandardCharsets.UTF_8);
        writer.println(json);
        writer.flush();

        // 3. Read back from the stream (simulating stdin)
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        BufferedReader reader = new BufferedReader(new InputStreamReader(bais, StandardCharsets.UTF_8));
        String readLine = reader.readLine();

        assertNotNull(readLine, "Line read from stream must not be null");

        // 4. Deserialize the read line back to a message
        JsonRpcMessage restored = serializer.deserialize(readLine);

        // 5. Verify equivalence
        assertEquals(original.jsonrpc(), restored.jsonrpc(),
                "jsonrpc must survive stdio round-trip");
        assertEquals(original.method(), restored.method(),
                "method must survive stdio round-trip");

        // id comparison (Jackson may change numeric types)
        if (original.id() != null) {
            assertEquals(original.id().toString(), restored.id().toString(),
                    "id must survive stdio round-trip (string representation)");
        } else {
            assertNull(restored.id(), "null id must remain null after stdio round-trip");
        }

        // result comparison
        if (original.result() != null) {
            assertEquals(original.result().toString(), restored.result().toString(),
                    "result must survive stdio round-trip (string representation)");
        } else {
            assertNull(restored.result(), "null result must remain null after stdio round-trip");
        }

        // error comparison
        if (original.error() != null) {
            assertNotNull(restored.error(), "error must not be null after stdio round-trip");
            assertEquals(original.error().code(), restored.error().code(),
                    "error code must survive stdio round-trip");
            assertEquals(original.error().message(), restored.error().message(),
                    "error message must survive stdio round-trip");
        } else {
            assertNull(restored.error(), "null error must remain null after stdio round-trip");
        }
    }
}
