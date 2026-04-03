/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.jsonrpc;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link JsonRpcSerializer}.
 *
 * <p>Covers Property 21 (round-trip serialization), Property 22 (null field omission),
 * and Property 23 (mutual exclusivity of error/result).</p>
 */
class JsonRpcSerializerPropertyTest {

    private final JsonRpcSerializer serializer = new JsonRpcSerializer();

    // -----------------------------------------------------------------------
    // Arbitraries
    // -----------------------------------------------------------------------

    /** Generates a valid JSON-RPC id (Integer or String). */
    @Provide
    Arbitrary<Object> jsonRpcIds() {
        Arbitrary<Object> intIds = Arbitraries.integers().between(1, 100_000).map(i -> (Object) i);
        Arbitrary<Object> stringIds = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).map(s -> (Object) s);
        return Arbitraries.oneOf(intIds, stringIds);
    }

    /** Generates simple result values (String or Integer) for round-trip safety. */
    @Provide
    Arbitrary<Object> simpleResults() {
        Arbitrary<Object> strings = Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30).map(s -> (Object) s);
        Arbitrary<Object> ints = Arbitraries.integers().between(-10_000, 10_000).map(i -> (Object) i);
        return Arbitraries.oneOf(strings, ints);
    }

    /** Generates a valid JsonRpcError. */
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
        // data is either null or a simple string
        Arbitrary<Object> data = Arbitraries.oneOf(
                Arbitraries.just(null),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).map(s -> (Object) s)
        );
        return Combinators.combine(codes, messages, data).as(JsonRpcError::new);
    }

    /** Generates valid JsonRpcMessage instances: success responses, error responses, and requests. */
    @Provide
    Arbitrary<JsonRpcMessage> validMessages() {
        // Success response: has id and result, no error
        Arbitrary<JsonRpcMessage> successMessages = Combinators.combine(
                jsonRpcIds(), simpleResults()
        ).as(JsonRpcMessage::success);

        // Error response: has id and error, no result
        Arbitrary<JsonRpcMessage> errorMessages = Combinators.combine(
                jsonRpcIds(), jsonRpcErrors()
        ).as(JsonRpcMessage::error);

        // Request: has method, id, optional params (null for simplicity)
        Arbitrary<JsonRpcMessage> requestMessages = Combinators.combine(
                Arbitraries.of("tools/list", "tools/call", "resources/list", "resources/read",
                        "prompts/list", "prompts/get", "initialize"),
                jsonRpcIds()
        ).as((method, id) -> new JsonRpcMessage("2.0", method, null, id, null, null));

        return Arbitraries.oneOf(successMessages, errorMessages, requestMessages);
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 21: Round-trip de serialização JsonRpcMessage
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 21: Round-trip de serialização JsonRpcMessage
    /**
     * For every valid JsonRpcMessage, serializing to JSON and deserializing back
     * must produce an equivalent object.
     *
     * <p><b>Validates: Requirements 18.4</b></p>
     */
    @Property(tries = 100)
    void roundTripSerializationProducesEquivalentMessage(
            @ForAll("validMessages") JsonRpcMessage original) {

        String json = serializer.serialize(original);
        JsonRpcMessage restored = serializer.deserialize(json);

        assertEquals(original.jsonrpc(), restored.jsonrpc(), "jsonrpc must survive round-trip");
        assertEquals(original.method(), restored.method(), "method must survive round-trip");

        // id comparison: Jackson may deserialize Integer ids as Integer
        if (original.id() != null) {
            assertEquals(original.id().toString(), restored.id().toString(),
                    "id must survive round-trip (string representation)");
        } else {
            assertNull(restored.id(), "null id must remain null after round-trip");
        }

        // result comparison
        if (original.result() != null) {
            assertEquals(original.result().toString(), restored.result().toString(),
                    "result must survive round-trip (string representation)");
        } else {
            assertNull(restored.result(), "null result must remain null after round-trip");
        }

        // error comparison
        if (original.error() != null) {
            assertNotNull(restored.error(), "error must not be null after round-trip");
            assertEquals(original.error().code(), restored.error().code(),
                    "error code must survive round-trip");
            assertEquals(original.error().message(), restored.error().message(),
                    "error message must survive round-trip");
            if (original.error().data() != null) {
                assertEquals(original.error().data().toString(), restored.error().data().toString(),
                        "error data must survive round-trip (string representation)");
            } else {
                assertNull(restored.error().data(), "null error data must remain null after round-trip");
            }
        } else {
            assertNull(restored.error(), "null error must remain null after round-trip");
        }

        // params comparison
        if (original.params() != null) {
            assertNotNull(restored.params(), "params must not be null after round-trip");
        } else {
            assertNull(restored.params(), "null params must remain null after round-trip");
        }
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 22: Campos null omitidos na serialização
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 22: Campos null omitidos na serialização
    /**
     * For every message with null fields, the serialized JSON string must NOT
     * contain the keys corresponding to those null fields.
     *
     * <p><b>Validates: Requirements 18.3</b></p>
     */
    @Property(tries = 100)
    void nullFieldsAreOmittedFromSerializedJson(
            @ForAll("validMessages") JsonRpcMessage message) {

        String json = serializer.serialize(message);

        if (message.method() == null) {
            assertFalse(json.contains("\"method\""),
                    "null 'method' must be omitted from JSON: " + json);
        }
        if (message.params() == null) {
            assertFalse(json.contains("\"params\""),
                    "null 'params' must be omitted from JSON: " + json);
        }
        if (message.id() == null) {
            assertFalse(json.contains("\"id\""),
                    "null 'id' must be omitted from JSON: " + json);
        }
        if (message.result() == null) {
            assertFalse(json.contains("\"result\""),
                    "null 'result' must be omitted from JSON: " + json);
        }
        if (message.error() == null) {
            assertFalse(json.contains("\"error\""),
                    "null 'error' must be omitted from JSON: " + json);
        }
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 23: Exclusividade mútua entre error e result
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 23: Exclusividade mútua entre error e result
    /**
     * For every valid message created via factory methods, if error is non-null
     * then result must be null, and vice-versa.
     *
     * <p><b>Validates: Requirements 18.5</b></p>
     */
    @Property(tries = 100)
    void errorAndResultAreMutuallyExclusive(
            @ForAll("validMessages") JsonRpcMessage message) {

        if (message.error() != null) {
            assertNull(message.result(),
                    "When error is present, result must be null. Got error="
                            + message.error() + ", result=" + message.result());
        }
        if (message.result() != null) {
            assertNull(message.error(),
                    "When result is present, error must be null. Got result="
                            + message.result() + ", error=" + message.error());
        }
    }
}
