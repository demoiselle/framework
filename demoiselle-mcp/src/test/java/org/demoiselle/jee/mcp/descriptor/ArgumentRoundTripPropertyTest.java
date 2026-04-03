/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.descriptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.jqwik.api.*;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.handler.McpJsonRpcHandler;
import org.demoiselle.jee.mcp.integration.PlainTextErrorFormatter;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcMessage;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;
import org.demoiselle.jee.mcp.registry.McpPromptRegistry;
import org.demoiselle.jee.mcp.registry.McpResourceRegistry;
import org.demoiselle.jee.mcp.registry.McpToolRegistry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for argument round-trip serialization.
 *
 * <p>Covers Property 8: For @McpTool methods with primitive/wrapper types,
 * deserializing JSON args and serializing result back preserves semantic equivalence.</p>
 */
class ArgumentRoundTripPropertyTest {

    private static final String SESSION = "rt-session";
    private final ObjectMapper objectMapper = new ObjectMapper();

    // ── Sample tool beans with primitive/wrapper types ───────────────────

    @SuppressWarnings("unused")
    public static class IntToolBean {
        public int addInts(@McpParam(name = "a") int a, @McpParam(name = "b") int b) {
            return a + b;
        }
    }

    @SuppressWarnings("unused")
    public static class LongToolBean {
        public long multiplyLongs(@McpParam(name = "a") long a, @McpParam(name = "b") long b) {
            return a * b;
        }
    }

    @SuppressWarnings("unused")
    public static class DoubleToolBean {
        public double addDoubles(@McpParam(name = "a") double a, @McpParam(name = "b") double b) {
            return a + b;
        }
    }

    @SuppressWarnings("unused")
    public static class BooleanToolBean {
        public boolean andBooleans(@McpParam(name = "a") boolean a, @McpParam(name = "b") boolean b) {
            return a && b;
        }
    }

    @SuppressWarnings("unused")
    public static class StringToolBean {
        public String concatStrings(@McpParam(name = "a") String a, @McpParam(name = "b") String b) {
            return a + b;
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────────

    private McpJsonRpcHandler newHandler(McpToolRegistry toolReg) {
        return new McpJsonRpcHandler(toolReg, new McpResourceRegistry(),
                new McpPromptRegistry(), new JsonRpcSerializer(),
                new McpConfig("test", "1.0.0"), new PlainTextErrorFormatter());
    }

    private void doHandshake(McpJsonRpcHandler handler) {
        handler.handle(SESSION,
                new JsonRpcMessage("2.0", "initialize", null, "init", null, null));
        handler.handle(SESSION,
                new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));
    }

    @SuppressWarnings("unchecked")
    private String callToolAndGetText(McpJsonRpcHandler handler, String toolName,
                                       Map<String, Object> arguments) {
        JsonRpcMessage req = new JsonRpcMessage("2.0", "tools/call",
                Map.of("name", toolName, "arguments", arguments), 1, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNull(resp.error(), "tools/call should not return JSON-RPC error");

        Map<String, Object> result = (Map<String, Object>) resp.result();
        assertEquals(false, result.get("isError"), "isError must be false");

        List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
        return content.get(0).get("text").toString();
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 8: Round-trip de serialização de argumentos e resultados
    // -----------------------------------------------------------------------

    /**
     * For int args: deserializing JSON args and serializing result preserves semantic equivalence.
     *
     * <p><b>Validates: Requirements 3.6, 16.8</b></p>
     */
    @Property(tries = 100)
    void intArgumentRoundTrip(@ForAll int a, @ForAll int b) throws Exception {
        McpToolRegistry toolReg = new McpToolRegistry();
        Method m = IntToolBean.class.getMethod("addInts", int.class, int.class);
        toolReg.register(new ToolDescriptor("addInts", "Add ints",
                Map.of("type", "object"), new IntToolBean(), m));

        McpJsonRpcHandler handler = newHandler(toolReg);
        doHandshake(handler);

        String text = callToolAndGetText(handler, "addInts", Map.of("a", a, "b", b));
        int deserialized = objectMapper.readValue(text, int.class);
        assertEquals(a + b, deserialized,
                "Round-trip must preserve int result for a=" + a + ", b=" + b);
    }

    /**
     * For long args: round-trip preserves semantic equivalence.
     *
     * <p><b>Validates: Requirements 3.6, 16.8</b></p>
     */
    @Property(tries = 100)
    void longArgumentRoundTrip(@ForAll @net.jqwik.api.constraints.LongRange(min = -1_000_000, max = 1_000_000) long a,
                                @ForAll @net.jqwik.api.constraints.LongRange(min = -1_000, max = 1_000) long b) throws Exception {
        McpToolRegistry toolReg = new McpToolRegistry();
        Method m = LongToolBean.class.getMethod("multiplyLongs", long.class, long.class);
        toolReg.register(new ToolDescriptor("multiplyLongs", "Multiply longs",
                Map.of("type", "object"), new LongToolBean(), m));

        McpJsonRpcHandler handler = newHandler(toolReg);
        doHandshake(handler);

        String text = callToolAndGetText(handler, "multiplyLongs", Map.of("a", a, "b", b));
        long deserialized = objectMapper.readValue(text, long.class);
        assertEquals(a * b, deserialized,
                "Round-trip must preserve long result for a=" + a + ", b=" + b);
    }

    /**
     * For double args: round-trip preserves semantic equivalence.
     *
     * <p><b>Validates: Requirements 3.6, 16.8</b></p>
     */
    @Property(tries = 100)
    void doubleArgumentRoundTrip(
            @ForAll @net.jqwik.api.constraints.DoubleRange(min = -1e6, max = 1e6) double a,
            @ForAll @net.jqwik.api.constraints.DoubleRange(min = -1e6, max = 1e6) double b) throws Exception {
        McpToolRegistry toolReg = new McpToolRegistry();
        Method m = DoubleToolBean.class.getMethod("addDoubles", double.class, double.class);
        toolReg.register(new ToolDescriptor("addDoubles", "Add doubles",
                Map.of("type", "object"), new DoubleToolBean(), m));

        McpJsonRpcHandler handler = newHandler(toolReg);
        doHandshake(handler);

        String text = callToolAndGetText(handler, "addDoubles", Map.of("a", a, "b", b));
        double deserialized = objectMapper.readValue(text, double.class);
        assertEquals(a + b, deserialized, 1e-9,
                "Round-trip must preserve double result for a=" + a + ", b=" + b);
    }

    /**
     * For boolean args: round-trip preserves semantic equivalence.
     *
     * <p><b>Validates: Requirements 3.6, 16.8</b></p>
     */
    @Property(tries = 100)
    void booleanArgumentRoundTrip(@ForAll boolean a, @ForAll boolean b) throws Exception {
        McpToolRegistry toolReg = new McpToolRegistry();
        Method m = BooleanToolBean.class.getMethod("andBooleans", boolean.class, boolean.class);
        toolReg.register(new ToolDescriptor("andBooleans", "AND booleans",
                Map.of("type", "object"), new BooleanToolBean(), m));

        McpJsonRpcHandler handler = newHandler(toolReg);
        doHandshake(handler);

        String text = callToolAndGetText(handler, "andBooleans", Map.of("a", a, "b", b));
        boolean deserialized = objectMapper.readValue(text, boolean.class);
        assertEquals(a && b, deserialized,
                "Round-trip must preserve boolean result for a=" + a + ", b=" + b);
    }

    /**
     * For String args: round-trip preserves semantic equivalence.
     *
     * <p><b>Validates: Requirements 3.6, 16.8</b></p>
     */
    @Property(tries = 100)
    void stringArgumentRoundTrip(@ForAll String a, @ForAll String b) throws Exception {
        McpToolRegistry toolReg = new McpToolRegistry();
        Method m = StringToolBean.class.getMethod("concatStrings", String.class, String.class);
        toolReg.register(new ToolDescriptor("concatStrings", "Concat strings",
                Map.of("type", "object"), new StringToolBean(), m));

        McpJsonRpcHandler handler = newHandler(toolReg);
        doHandshake(handler);

        String text = callToolAndGetText(handler, "concatStrings", Map.of("a", a, "b", b));
        // Jackson serializes strings with quotes, so we deserialize
        String deserialized = objectMapper.readValue(text, String.class);
        assertEquals(a + b, deserialized,
                "Round-trip must preserve String result");
    }
}
