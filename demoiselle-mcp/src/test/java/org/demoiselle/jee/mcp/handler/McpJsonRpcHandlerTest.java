/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.handler;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link McpJsonRpcHandler}.
 */
class McpJsonRpcHandlerTest {

    private McpToolRegistry toolRegistry;
    private McpResourceRegistry resourceRegistry;
    private McpPromptRegistry promptRegistry;
    private JsonRpcSerializer serializer;
    private McpConfig config;
    private McpJsonRpcHandler handler;

    @BeforeEach
    void setUp() {
        toolRegistry = new McpToolRegistry();
        resourceRegistry = new McpResourceRegistry();
        promptRegistry = new McpPromptRegistry();
        serializer = new JsonRpcSerializer();
        config = new McpConfig("test-server", "1.0.0");
        handler = new McpJsonRpcHandler(
                toolRegistry, resourceRegistry, promptRegistry,
                serializer, config, new PlainTextErrorFormatter());
    }

    // ── Test helper beans ───────────────────────────────────────────────

    @SuppressWarnings("unused")
    public static class SampleToolBean {
        public String greet(@McpParam(name = "name") String name) {
            return "Hello, " + name + "!";
        }

        public int add(@McpParam(name = "a") int a, @McpParam(name = "b") int b) {
            return a + b;
        }

        public String failingTool(@McpParam(name = "input") String input) {
            throw new RuntimeException("Tool execution failed");
        }
    }

    @SuppressWarnings("unused")
    public static class SampleResourceBean {
        public String readConfig() {
            return "server.port=8080";
        }
    }

    @SuppressWarnings("unused")
    public static class SamplePromptBean {
        public List<Map<String, Object>> codeReview(@McpParam(name = "code") String code) {
            return List.of(Map.of(
                    "role", "user",
                    "content", Map.of("type", "text", "text", "Review: " + code)
            ));
        }
    }

    // ── 6.1: Core routing and validation ────────────────────────────────

    @Nested
    @DisplayName("6.1 - Core routing and validation")
    class CoreRoutingTests {

        @Test
        @DisplayName("Invalid jsonrpc version returns -32600")
        void invalidJsonRpcVersion() {
            var req = new JsonRpcMessage("1.0", "initialize", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code());
            assertEquals(1, resp.id());
        }

        @Test
        @DisplayName("Missing jsonrpc field returns -32600")
        void missingJsonRpcField() {
            var req = new JsonRpcMessage(null, "initialize", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code());
        }

        @Test
        @DisplayName("Unknown method returns -32601")
        void unknownMethod() {
            var req = new JsonRpcMessage("2.0", "unknown/method", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.METHOD_NOT_FOUND, resp.error().code());
            assertTrue(resp.error().message().contains("unknown/method"));
        }

        @Test
        @DisplayName("Missing method field returns -32600")
        void missingMethodField() {
            var req = new JsonRpcMessage("2.0", null, null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code());
        }

        @Test
        @DisplayName("Notification (no id) returns null")
        void notificationReturnsNull() {
            var req = new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null);
            var resp = handler.handle("session1", req);

            assertNull(resp);
        }

        @Test
        @DisplayName("Response id matches request id")
        void responseIdMatchesRequestId() {
            var req = new JsonRpcMessage("2.0", "initialize", null, 42, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertEquals(42, resp.id());
        }

        @Test
        @DisplayName("Response id matches string request id")
        void responseIdMatchesStringRequestId() {
            var req = new JsonRpcMessage("2.0", "initialize", null, "req-abc", null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertEquals("req-abc", resp.id());
        }

        @Test
        @DisplayName("tools/call before initialization returns -32600")
        void toolsCallBeforeInit() {
            var req = new JsonRpcMessage("2.0", "tools/call",
                    Map.of("name", "greet"), 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code());
            assertTrue(resp.error().message().contains("not initialized"));
        }

        @Test
        @DisplayName("resources/read before initialization returns -32600")
        void resourcesReadBeforeInit() {
            var req = new JsonRpcMessage("2.0", "resources/read",
                    Map.of("uri", "config://app"), 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code());
        }

        @Test
        @DisplayName("prompts/get before initialization returns -32600")
        void promptsGetBeforeInit() {
            var req = new JsonRpcMessage("2.0", "prompts/get",
                    Map.of("name", "review"), 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_REQUEST, resp.error().code());
        }

        @Test
        @DisplayName("tools/list does NOT require initialization")
        void toolsListDoesNotRequireInit() {
            var req = new JsonRpcMessage("2.0", "tools/list", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
        }
    }

    // ── 6.2: initialize / initialized ───────────────────────────────────

    @Nested
    @DisplayName("6.2 - initialize / initialized")
    class InitializeTests {

        @Test
        @DisplayName("initialize returns protocolVersion, serverInfo, and capabilities")
        void initializeReturnsExpectedFields() {
            var req = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            assertNotNull(resp.result());

            @SuppressWarnings("unchecked")
            var result = (Map<String, Object>) resp.result();
            assertNotNull(result.get("protocolVersion"));

            @SuppressWarnings("unchecked")
            var serverInfo = (Map<String, Object>) result.get("serverInfo");
            assertEquals("test-server", serverInfo.get("name"));
            assertEquals("1.0.0", serverInfo.get("version"));

            assertNotNull(result.get("capabilities"));
        }

        @Test
        @DisplayName("capabilities includes tools when tools are registered")
        @SuppressWarnings("unchecked")
        void capabilitiesIncludesToolsWhenRegistered() throws Exception {
            Method m = SampleToolBean.class.getMethod("greet", String.class);
            toolRegistry.register(new ToolDescriptor("greet", "Greets", Map.of(), new SampleToolBean(), m));

            var req = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
            var resp = handler.handle("session1", req);
            var result = (Map<String, Object>) resp.result();
            var caps = (Map<String, Object>) result.get("capabilities");

            assertTrue(caps.containsKey("tools"));
        }

        @Test
        @DisplayName("capabilities excludes tools when none registered")
        @SuppressWarnings("unchecked")
        void capabilitiesExcludesToolsWhenEmpty() {
            var req = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
            var resp = handler.handle("session1", req);
            var result = (Map<String, Object>) resp.result();
            var caps = (Map<String, Object>) result.get("capabilities");

            assertFalse(caps.containsKey("tools"));
        }

        @Test
        @DisplayName("capabilities includes resources when resources are registered")
        @SuppressWarnings("unchecked")
        void capabilitiesIncludesResourcesWhenRegistered() throws Exception {
            Method m = SampleResourceBean.class.getMethod("readConfig");
            resourceRegistry.register(new ResourceDescriptor(
                    "config://app", "App Config", "Config", "text/plain",
                    new SampleResourceBean(), m));

            var req = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
            var resp = handler.handle("session1", req);
            var result = (Map<String, Object>) resp.result();
            var caps = (Map<String, Object>) result.get("capabilities");

            assertTrue(caps.containsKey("resources"));
        }

        @Test
        @DisplayName("capabilities includes prompts when prompts are registered")
        @SuppressWarnings("unchecked")
        void capabilitiesIncludesPromptsWhenRegistered() throws Exception {
            Method m = SamplePromptBean.class.getMethod("codeReview", String.class);
            promptRegistry.register(new PromptDescriptor(
                    "code-review", "Review code", List.of(), new SamplePromptBean(), m));

            var req = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
            var resp = handler.handle("session1", req);
            var result = (Map<String, Object>) resp.result();
            var caps = (Map<String, Object>) result.get("capabilities");

            assertTrue(caps.containsKey("prompts"));
        }

        @Test
        @DisplayName("initialized notification marks session as active")
        void initializedMarksSession() {
            // Send initialized notification
            var notif = new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null);
            var resp = handler.handle("session1", notif);
            assertNull(resp); // notification → null

            // Now tools/call should not fail with session error (may fail with tool not found)
            var req = new JsonRpcMessage("2.0", "tools/call",
                    Map.of("name", "nonexistent"), 1, null, null);
            var resp2 = handler.handle("session1", req);

            // Should get INVALID_PARAMS (tool not found), not INVALID_REQUEST (session)
            assertNotNull(resp2);
            assertNotNull(resp2.error());
            assertEquals(JsonRpcError.INVALID_PARAMS, resp2.error().code());
        }
    }

    // ── 6.3: tools/list and tools/call ──────────────────────────────────

    @Nested
    @DisplayName("6.3 - tools/list and tools/call")
    class ToolsTests {

        @BeforeEach
        void initSession() {
            handler.markSessionInitialized("session1");
        }

        @Test
        @DisplayName("tools/list returns registered tools")
        @SuppressWarnings("unchecked")
        void toolsListReturnsRegisteredTools() throws Exception {
            Method m = SampleToolBean.class.getMethod("greet", String.class);
            toolRegistry.register(new ToolDescriptor("greet", "Greets a person",
                    Map.of("type", "object"), new SampleToolBean(), m));

            var req = new JsonRpcMessage("2.0", "tools/list", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            var result = (Map<String, Object>) resp.result();
            var tools = (List<Map<String, Object>>) result.get("tools");
            assertEquals(1, tools.size());
            assertEquals("greet", tools.get(0).get("name"));
            assertEquals("Greets a person", tools.get(0).get("description"));
        }

        @Test
        @DisplayName("tools/list returns empty list when no tools registered")
        @SuppressWarnings("unchecked")
        void toolsListReturnsEmptyList() {
            var req = new JsonRpcMessage("2.0", "tools/list", null, 1, null, null);
            var resp = handler.handle("session1", req);

            var result = (Map<String, Object>) resp.result();
            var tools = (List<?>) result.get("tools");
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("tools/call invokes method and returns result")
        @SuppressWarnings("unchecked")
        void toolsCallSuccess() throws Exception {
            Method m = SampleToolBean.class.getMethod("greet", String.class);
            toolRegistry.register(new ToolDescriptor("greet", "Greets",
                    Map.of("type", "object"), new SampleToolBean(), m));

            var req = new JsonRpcMessage("2.0", "tools/call",
                    Map.of("name", "greet", "arguments", Map.of("name", "World")),
                    1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            var result = (Map<String, Object>) resp.result();
            assertEquals(false, result.get("isError"));
            var content = (List<Map<String, Object>>) result.get("content");
            assertEquals(1, content.size());
            assertEquals("text", content.get(0).get("type"));
            String text = content.get(0).get("text").toString();
            assertTrue(text.contains("Hello, World!"));
        }

        @Test
        @DisplayName("tools/call with numeric args invokes correctly")
        @SuppressWarnings("unchecked")
        void toolsCallWithNumericArgs() throws Exception {
            Method m = SampleToolBean.class.getMethod("add", int.class, int.class);
            toolRegistry.register(new ToolDescriptor("add", "Adds two numbers",
                    Map.of("type", "object"), new SampleToolBean(), m));

            var req = new JsonRpcMessage("2.0", "tools/call",
                    Map.of("name", "add", "arguments", Map.of("a", 3, "b", 7)),
                    1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            var result = (Map<String, Object>) resp.result();
            assertEquals(false, result.get("isError"));
            var content = (List<Map<String, Object>>) result.get("content");
            assertEquals("10", content.get(0).get("text").toString());
        }

        @Test
        @DisplayName("tools/call with nonexistent tool returns -32602")
        void toolsCallNotFound() {
            var req = new JsonRpcMessage("2.0", "tools/call",
                    Map.of("name", "nonexistent"), 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_PARAMS, resp.error().code());
            assertTrue(resp.error().message().contains("nonexistent"));
        }

        @Test
        @DisplayName("tools/call with exception sets isError=true")
        @SuppressWarnings("unchecked")
        void toolsCallException() throws Exception {
            Method m = SampleToolBean.class.getMethod("failingTool", String.class);
            toolRegistry.register(new ToolDescriptor("failing", "Fails",
                    Map.of("type", "object"), new SampleToolBean(), m));

            var req = new JsonRpcMessage("2.0", "tools/call",
                    Map.of("name", "failing", "arguments", Map.of("input", "test")),
                    1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error()); // Error is in the result, not in JSON-RPC error
            var result = (Map<String, Object>) resp.result();
            assertEquals(true, result.get("isError"));
            var content = (List<Map<String, Object>>) result.get("content");
            assertTrue(content.get(0).get("text").toString().contains("Tool execution failed"));
        }
    }

    // ── 6.4: resources and prompts ──────────────────────────────────────

    @Nested
    @DisplayName("6.4 - resources/list, resources/read, prompts/list, prompts/get")
    class ResourcesAndPromptsTests {

        @BeforeEach
        void initSession() {
            handler.markSessionInitialized("session1");
        }

        @Test
        @DisplayName("resources/list returns registered resources")
        @SuppressWarnings("unchecked")
        void resourcesListReturnsRegistered() throws Exception {
            Method m = SampleResourceBean.class.getMethod("readConfig");
            resourceRegistry.register(new ResourceDescriptor(
                    "config://app", "App Config", "Application config",
                    "text/plain", new SampleResourceBean(), m));

            var req = new JsonRpcMessage("2.0", "resources/list", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            var result = (Map<String, Object>) resp.result();
            var resources = (List<Map<String, Object>>) result.get("resources");
            assertEquals(1, resources.size());
            assertEquals("config://app", resources.get(0).get("uri"));
            assertEquals("App Config", resources.get(0).get("name"));
        }

        @Test
        @DisplayName("resources/read invokes method and returns content")
        @SuppressWarnings("unchecked")
        void resourcesReadSuccess() throws Exception {
            Method m = SampleResourceBean.class.getMethod("readConfig");
            resourceRegistry.register(new ResourceDescriptor(
                    "config://app", "App Config", "", "text/plain",
                    new SampleResourceBean(), m));

            var req = new JsonRpcMessage("2.0", "resources/read",
                    Map.of("uri", "config://app"), 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            var result = (Map<String, Object>) resp.result();
            var contents = (List<Map<String, Object>>) result.get("contents");
            assertEquals(1, contents.size());
            assertEquals("config://app", contents.get(0).get("uri"));
            assertEquals("text/plain", contents.get(0).get("mimeType"));
            assertEquals("server.port=8080", contents.get(0).get("text"));
        }

        @Test
        @DisplayName("resources/read with nonexistent URI returns -32602")
        void resourcesReadNotFound() {
            var req = new JsonRpcMessage("2.0", "resources/read",
                    Map.of("uri", "nonexistent://uri"), 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_PARAMS, resp.error().code());
        }

        @Test
        @DisplayName("prompts/list returns registered prompts with arguments")
        @SuppressWarnings("unchecked")
        void promptsListReturnsRegistered() throws Exception {
            Method m = SamplePromptBean.class.getMethod("codeReview", String.class);
            promptRegistry.register(new PromptDescriptor(
                    "code-review", "Review code",
                    List.of(new PromptArgument("code", "Code to review", true)),
                    new SamplePromptBean(), m));

            var req = new JsonRpcMessage("2.0", "prompts/list", null, 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            var result = (Map<String, Object>) resp.result();
            var prompts = (List<Map<String, Object>>) result.get("prompts");
            assertEquals(1, prompts.size());
            assertEquals("code-review", prompts.get(0).get("name"));

            var args = (List<Map<String, Object>>) prompts.get(0).get("arguments");
            assertEquals(1, args.size());
            assertEquals("code", args.get(0).get("name"));
            assertEquals(true, args.get(0).get("required"));
        }

        @Test
        @DisplayName("prompts/get invokes method and returns messages")
        @SuppressWarnings("unchecked")
        void promptsGetSuccess() throws Exception {
            Method m = SamplePromptBean.class.getMethod("codeReview", String.class);
            promptRegistry.register(new PromptDescriptor(
                    "code-review", "Review code",
                    List.of(new PromptArgument("code", "Code to review", true)),
                    new SamplePromptBean(), m));

            var req = new JsonRpcMessage("2.0", "prompts/get",
                    Map.of("name", "code-review", "arguments", Map.of("code", "x = 1")),
                    1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNull(resp.error());
            var result = (Map<String, Object>) resp.result();
            var messages = (List<?>) result.get("messages");
            assertNotNull(messages);
            assertFalse(messages.isEmpty());
        }

        @Test
        @DisplayName("prompts/get with nonexistent prompt returns -32602")
        void promptsGetNotFound() {
            var req = new JsonRpcMessage("2.0", "prompts/get",
                    Map.of("name", "nonexistent"), 1, null, null);
            var resp = handler.handle("session1", req);

            assertNotNull(resp);
            assertNotNull(resp.error());
            assertEquals(JsonRpcError.INVALID_PARAMS, resp.error().code());
        }
    }
}
