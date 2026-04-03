/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.handler;

import net.jqwik.api.*;
import net.jqwik.api.constraints.IntRange;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.descriptor.PromptArgument;
import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;
import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;
import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;
import org.demoiselle.jee.mcp.integration.PlainTextErrorFormatter;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcError;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcMessage;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;
import org.demoiselle.jee.mcp.registry.McpPromptRegistry;
import org.demoiselle.jee.mcp.registry.McpResourceRegistry;
import org.demoiselle.jee.mcp.registry.McpToolRegistry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link McpJsonRpcHandler}.
 */
class McpJsonRpcHandlerPropertyTest {

    private static final String SESSION = "test-session";

    private static final Set<String> SUPPORTED_METHODS = Set.of(
            "initialize", "notifications/initialized",
            "tools/list", "tools/call",
            "resources/list", "resources/read",
            "prompts/list", "prompts/get"
    );

    private static final Set<String> METHODS_REQUIRING_INIT = Set.of(
            "tools/call", "resources/read", "prompts/get"
    );

    // ── Sample beans for testing ────────────────────────────────────────

    @SuppressWarnings("unused")
    public static class SucceedingToolBean {
        public String echo(@McpParam(name = "input") String input) {
            return "echo:" + input;
        }
    }

    @SuppressWarnings("unused")
    public static class FailingToolBean {
        public String fail(@McpParam(name = "input") String input) {
            throw new RuntimeException("Intentional failure");
        }
    }

    @SuppressWarnings("unused")
    public static class SampleResourceBean {
        public String read() {
            return "resource-content";
        }
    }

    @SuppressWarnings("unused")
    public static class SamplePromptBean {
        public List<Map<String, Object>> generate(@McpParam(name = "topic") String topic) {
            return List.of(Map.of("role", "user",
                    "content", Map.of("type", "text", "text", "About: " + topic)));
        }
    }

    // ── Helper methods ──────────────────────────────────────────────────

    private McpJsonRpcHandler newHandler(McpToolRegistry toolReg,
                                         McpResourceRegistry resReg,
                                         McpPromptRegistry promptReg) {
        return new McpJsonRpcHandler(toolReg, resReg, promptReg,
                new JsonRpcSerializer(), new McpConfig("test", "1.0.0"),
                new PlainTextErrorFormatter());
    }

    private void doHandshake(McpJsonRpcHandler handler, String sessionId) {
        handler.handle(sessionId,
                new JsonRpcMessage("2.0", "initialize", null, "init-id", null, null));
        handler.handle(sessionId,
                new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));
    }

    // ── Arbitraries ─────────────────────────────────────────────────────

    @Provide
    Arbitrary<String> randomNames() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(40);
    }

    @Provide
    Arbitrary<String> randomUris() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
                .map(s -> "test://" + s);
    }

    @Provide
    Arbitrary<Object> requestIds() {
        return Arbitraries.oneOf(
                Arbitraries.integers().between(1, 100_000).map(i -> (Object) i),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20).map(s -> (Object) s)
        );
    }

    @Provide
    Arbitrary<String> nonJsonRpc20Strings() {
        return Arbitraries.strings().ofMinLength(0).ofMaxLength(20)
                .filter(s -> !"2.0".equals(s));
    }

    @Provide
    Arbitrary<String> unsupportedMethods() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30)
                .filter(s -> !SUPPORTED_METHODS.contains(s));
    }

    @Provide
    Arbitrary<String> protectedMethods() {
        return Arbitraries.of("tools/call", "resources/read", "prompts/get");
    }

    @Provide
    Arbitrary<String> anyKnownMethod() {
        return Arbitraries.of(
                "initialize", "notifications/initialized",
                "tools/list", "tools/call",
                "resources/list", "resources/read",
                "prompts/list", "prompts/get"
        );
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 6: Lookup inexistente retorna -32602
    // -----------------------------------------------------------------------

    /**
     * For any unregistered tool name, tools/call must return error -32602.
     *
     * <p><b>Validates: Requirements 3.2</b></p>
     */
    @Property(tries = 100)
    void unregisteredToolNameReturnsInvalidParams(@ForAll("randomNames") String toolName) {
        McpToolRegistry toolReg = new McpToolRegistry();
        McpJsonRpcHandler handler = newHandler(toolReg, new McpResourceRegistry(), new McpPromptRegistry());
        doHandshake(handler, SESSION);

        JsonRpcMessage req = new JsonRpcMessage("2.0", "tools/call",
                Map.of("name", toolName), 1, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNotNull(resp.error());
        assertEquals(JsonRpcError.INVALID_PARAMS, resp.error().code(),
                "Unregistered tool '" + toolName + "' must return -32602");
    }

    /**
     * For any unregistered resource URI, resources/read must return error -32602.
     *
     * <p><b>Validates: Requirements 8.5</b></p>
     */
    @Property(tries = 100)
    void unregisteredResourceUriReturnsInvalidParams(@ForAll("randomUris") String uri) {
        McpResourceRegistry resReg = new McpResourceRegistry();
        McpJsonRpcHandler handler = newHandler(new McpToolRegistry(), resReg, new McpPromptRegistry());
        doHandshake(handler, SESSION);

        JsonRpcMessage req = new JsonRpcMessage("2.0", "resources/read",
                Map.of("uri", uri), 1, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNotNull(resp.error());
        assertEquals(JsonRpcError.INVALID_PARAMS, resp.error().code(),
                "Unregistered resource URI '" + uri + "' must return -32602");
    }

    /**
     * For any unregistered prompt name, prompts/get must return error -32602.
     *
     * <p><b>Validates: Requirements 9.5</b></p>
     */
    @Property(tries = 100)
    void unregisteredPromptNameReturnsInvalidParams(@ForAll("randomNames") String promptName) {
        McpPromptRegistry promptReg = new McpPromptRegistry();
        McpJsonRpcHandler handler = newHandler(new McpToolRegistry(), new McpResourceRegistry(), promptReg);
        doHandshake(handler, SESSION);

        JsonRpcMessage req = new JsonRpcMessage("2.0", "prompts/get",
                Map.of("name", promptName), 1, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNotNull(resp.error());
        assertEquals(JsonRpcError.INVALID_PARAMS, resp.error().code(),
                "Unregistered prompt '" + promptName + "' must return -32602");
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 7: isError reflete exceção do método CDI
    // -----------------------------------------------------------------------

    /**
     * For tools/call: if the method throws, isError=true; if it succeeds, isError=false.
     *
     * <p><b>Validates: Requirements 3.4, 3.5</b></p>
     */
    @Property(tries = 100)
    void isErrorReflectsMethodException(@ForAll boolean shouldFail,
                                         @ForAll("randomNames") String input) throws Exception {
        McpToolRegistry toolReg = new McpToolRegistry();
        McpResourceRegistry resReg = new McpResourceRegistry();
        McpPromptRegistry promptReg = new McpPromptRegistry();

        Method succeedMethod = SucceedingToolBean.class.getMethod("echo", String.class);
        toolReg.register(new ToolDescriptor("succeed-tool", "Succeeds",
                Map.of("type", "object"), new SucceedingToolBean(), succeedMethod));

        Method failMethod = FailingToolBean.class.getMethod("fail", String.class);
        toolReg.register(new ToolDescriptor("fail-tool", "Fails",
                Map.of("type", "object"), new FailingToolBean(), failMethod));

        McpJsonRpcHandler handler = newHandler(toolReg, resReg, promptReg);
        doHandshake(handler, SESSION);

        String toolName = shouldFail ? "fail-tool" : "succeed-tool";
        JsonRpcMessage req = new JsonRpcMessage("2.0", "tools/call",
                Map.of("name", toolName, "arguments", Map.of("input", input)),
                1, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNull(resp.error(), "tools/call should not return JSON-RPC error for valid tool");

        @SuppressWarnings("unchecked")
        Map<String, Object> result = (Map<String, Object>) resp.result();
        assertNotNull(result);

        if (shouldFail) {
            assertEquals(true, result.get("isError"),
                    "isError must be true when method throws");
        } else {
            assertEquals(false, result.get("isError"),
                    "isError must be false when method succeeds");
        }
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 10: Capabilities refletem estado dos registros
    // -----------------------------------------------------------------------

    /**
     * initialize response includes "tools":{} iff tools registered,
     * "resources":{} iff resources registered, "prompts":{} iff prompts registered.
     *
     * <p><b>Validates: Requirements 6.1, 6.2, 6.3, 6.4</b></p>
     */
    @Property(tries = 100)
    @SuppressWarnings("unchecked")
    void capabilitiesReflectRegistryState(@ForAll @IntRange(min = 0, max = 3) int numTools,
                                           @ForAll @IntRange(min = 0, max = 3) int numResources,
                                           @ForAll @IntRange(min = 0, max = 3) int numPrompts) throws Exception {
        McpToolRegistry toolReg = new McpToolRegistry();
        McpResourceRegistry resReg = new McpResourceRegistry();
        McpPromptRegistry promptReg = new McpPromptRegistry();

        Method echoMethod = SucceedingToolBean.class.getMethod("echo", String.class);
        for (int i = 0; i < numTools; i++) {
            toolReg.register(new ToolDescriptor("tool-" + i, "Tool " + i,
                    Map.of("type", "object"), new SucceedingToolBean(), echoMethod));
        }

        Method readMethod = SampleResourceBean.class.getMethod("read");
        for (int i = 0; i < numResources; i++) {
            resReg.register(new ResourceDescriptor("res://" + i, "Res " + i,
                    "", "text/plain", new SampleResourceBean(), readMethod));
        }

        Method genMethod = SamplePromptBean.class.getMethod("generate", String.class);
        for (int i = 0; i < numPrompts; i++) {
            promptReg.register(new PromptDescriptor("prompt-" + i, "Prompt " + i,
                    List.of(), new SamplePromptBean(), genMethod));
        }

        McpJsonRpcHandler handler = newHandler(toolReg, resReg, promptReg);
        JsonRpcMessage req = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNull(resp.error());

        Map<String, Object> result = (Map<String, Object>) resp.result();
        assertNotNull(result.get("protocolVersion"), "Must contain protocolVersion");
        assertNotNull(result.get("serverInfo"), "Must contain serverInfo");

        Map<String, Object> caps = (Map<String, Object>) result.get("capabilities");
        assertNotNull(caps);

        assertEquals(numTools > 0, caps.containsKey("tools"),
                "capabilities must contain 'tools' iff tools registered (numTools=" + numTools + ")");
        assertEquals(numResources > 0, caps.containsKey("resources"),
                "capabilities must contain 'resources' iff resources registered (numResources=" + numResources + ")");
        assertEquals(numPrompts > 0, caps.containsKey("prompts"),
                "capabilities must contain 'prompts' iff prompts registered (numPrompts=" + numPrompts + ")");
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 11: Requisições pré-handshake rejeitadas com -32600
    // -----------------------------------------------------------------------

    /**
     * tools/call, resources/read, prompts/get before handshake → error -32600.
     *
     * <p><b>Validates: Requirements 6.6</b></p>
     */
    @Property(tries = 100)
    void preHandshakeRequestsRejected(@ForAll("protectedMethods") String method,
                                       @ForAll("requestIds") Object id) {
        McpJsonRpcHandler handler = newHandler(new McpToolRegistry(),
                new McpResourceRegistry(), new McpPromptRegistry());
        // No handshake performed

        JsonRpcMessage req = new JsonRpcMessage("2.0", method,
                Map.of("name", "anything", "uri", "any://uri"), id, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp, "Pre-handshake request must return a response");
        assertNotNull(resp.error(), "Pre-handshake request must return an error");
        assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code(),
                "Pre-handshake " + method + " must return -32600");
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 12: Validação JSON-RPC e consistência de id
    // -----------------------------------------------------------------------

    /**
     * jsonrpc absent or ≠ "2.0" → error -32600.
     *
     * <p><b>Validates: Requirements 7.1, 7.2</b></p>
     */
    @Property(tries = 100)
    void invalidJsonRpcVersionReturnsInvalidRequest(@ForAll("nonJsonRpc20Strings") String badVersion,
                                                     @ForAll("requestIds") Object id) {
        McpJsonRpcHandler handler = newHandler(new McpToolRegistry(),
                new McpResourceRegistry(), new McpPromptRegistry());

        JsonRpcMessage req = new JsonRpcMessage(badVersion, "initialize", null, id, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNotNull(resp.error());
        assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code(),
                "jsonrpc='" + badVersion + "' must return -32600");
    }

    /**
     * Response id = request id for all valid requests.
     *
     * <p><b>Validates: Requirements 7.5, 7.6</b></p>
     */
    @Property(tries = 100)
    void responseIdMatchesRequestId(@ForAll("requestIds") Object id) {
        McpJsonRpcHandler handler = newHandler(new McpToolRegistry(),
                new McpResourceRegistry(), new McpPromptRegistry());

        // Use "initialize" as a valid method that always works
        JsonRpcMessage req = new JsonRpcMessage("2.0", "initialize", null, id, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertEquals(id, resp.id(),
                "Response id must match request id");
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 13: Método desconhecido retorna -32601
    // -----------------------------------------------------------------------

    /**
     * Any method not in supported set → error -32601.
     *
     * <p><b>Validates: Requirements 7.3</b></p>
     */
    @Property(tries = 100)
    void unknownMethodReturnsMethodNotFound(@ForAll("unsupportedMethods") String method,
                                             @ForAll("requestIds") Object id) {
        McpJsonRpcHandler handler = newHandler(new McpToolRegistry(),
                new McpResourceRegistry(), new McpPromptRegistry());

        JsonRpcMessage req = new JsonRpcMessage("2.0", method, null, id, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNotNull(resp);
        assertNotNull(resp.error());
        assertEquals(JsonRpcError.METHOD_NOT_FOUND, resp.error().code(),
                "Unknown method '" + method + "' must return -32601");
        assertEquals(id, resp.id(), "Response id must match request id");
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 14: Notificações não produzem resposta
    // -----------------------------------------------------------------------

    /**
     * Any message without id → handler returns null.
     *
     * <p><b>Validates: Requirements 7.4</b></p>
     */
    @Property(tries = 100)
    void notificationsReturnNull(@ForAll("anyKnownMethod") String method) {
        McpJsonRpcHandler handler = newHandler(new McpToolRegistry(),
                new McpResourceRegistry(), new McpPromptRegistry());

        // id = null makes it a notification
        JsonRpcMessage req = new JsonRpcMessage("2.0", method, null, null, null, null);
        JsonRpcMessage resp = handler.handle(SESSION, req);

        assertNull(resp, "Notification (no id) for method '" + method + "' must return null");
    }
}
