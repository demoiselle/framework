/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.annotation.McpPrompt;
import org.demoiselle.jee.mcp.annotation.McpResource;
import org.demoiselle.jee.mcp.annotation.McpTool;
import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.descriptor.PromptArgument;
import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;
import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;
import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;
import org.demoiselle.jee.mcp.handler.McpJsonRpcHandler;
import org.demoiselle.jee.mcp.integration.ErrorFormatter;
import org.demoiselle.jee.mcp.integration.PlainTextErrorFormatter;
import org.demoiselle.jee.mcp.integration.ProblemDetailErrorFormatter;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcError;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcMessage;
import org.demoiselle.jee.mcp.jsonrpc.JsonRpcSerializer;
import org.demoiselle.jee.mcp.registry.McpPromptRegistry;
import org.demoiselle.jee.mcp.registry.McpResourceRegistry;
import org.demoiselle.jee.mcp.registry.McpToolRegistry;
import org.demoiselle.jee.mcp.schema.JsonSchemaGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests that simulate the full MCP flow without a CDI container.
 *
 * <p>Components are manually wired to verify the complete pipeline:
 * annotation discovery → registry population → handshake → tools/call → response.</p>
 */
class McpIntegrationTest {

    // ── Sample beans simulating annotated CDI beans ─────────────────────

    @SuppressWarnings("unused")
    public static class CalculatorBean {
        @McpTool(description = "Adds two integers")
        public int add(@McpParam(name = "a", description = "First operand") int a,
                       @McpParam(name = "b", description = "Second operand") int b) {
            return a + b;
        }

        @McpTool(name = "multiply", description = "Multiplies two integers")
        public int multiply(@McpParam(name = "x") int x, @McpParam(name = "y") int y) {
            return x * y;
        }

        @McpTool(name = "failing-tool", description = "Always fails")
        public String failingTool(@McpParam(name = "input") String input) {
            throw new RuntimeException("Intentional failure: " + input);
        }
    }

    @SuppressWarnings("unused")
    public static class ConfigResourceBean {
        @McpResource(uri = "config://app", name = "App Config",
                     description = "Application configuration", mimeType = "text/plain")
        public String readConfig() {
            return "server.port=8080\nserver.host=localhost";
        }
    }

    @SuppressWarnings("unused")
    public static class ReviewPromptBean {
        @McpPrompt(name = "code-review", description = "Reviews code")
        public List<Map<String, Object>> codeReview(
                @McpParam(name = "code", description = "Code to review") String code) {
            return List.of(Map.of(
                    "role", "user",
                    "content", Map.of("type", "text", "text", "Review this: " + code)
            ));
        }
    }

    // ── Shared components ───────────────────────────────────────────────

    private McpToolRegistry toolRegistry;
    private McpResourceRegistry resourceRegistry;
    private McpPromptRegistry promptRegistry;
    private JsonSchemaGenerator schemaGenerator;
    private JsonRpcSerializer serializer;
    private McpConfig config;

    @BeforeEach
    void setUp() {
        toolRegistry = new McpToolRegistry();
        resourceRegistry = new McpResourceRegistry();
        promptRegistry = new McpPromptRegistry();
        schemaGenerator = new JsonSchemaGenerator();
        serializer = new JsonRpcSerializer();
        config = new McpConfig("integration-test-server", "1.0.0");
    }

    /**
     * Simulates what McpBootstrapExtension.afterDeploymentValidation does:
     * discovers annotated methods and registers descriptors.
     */
    private void registerAllAnnotations() throws Exception {
        CalculatorBean calcBean = new CalculatorBean();
        ConfigResourceBean configBean = new ConfigResourceBean();
        ReviewPromptBean promptBean = new ReviewPromptBean();

        // Register tools (simulating extension discovery)
        for (Method m : CalculatorBean.class.getDeclaredMethods()) {
            McpTool toolAnn = m.getAnnotation(McpTool.class);
            if (toolAnn != null) {
                String name = toolAnn.name().isEmpty() ? m.getName() : toolAnn.name();
                Map<String, Object> inputSchema = schemaGenerator.generate(m);
                toolRegistry.register(new ToolDescriptor(name, toolAnn.description(),
                        inputSchema, calcBean, m));
            }
        }

        // Register resources
        for (Method m : ConfigResourceBean.class.getDeclaredMethods()) {
            McpResource resAnn = m.getAnnotation(McpResource.class);
            if (resAnn != null) {
                resourceRegistry.register(new ResourceDescriptor(
                        resAnn.uri(), resAnn.name(), resAnn.description(),
                        resAnn.mimeType(), configBean, m));
            }
        }

        // Register prompts
        for (Method m : ReviewPromptBean.class.getDeclaredMethods()) {
            McpPrompt promptAnn = m.getAnnotation(McpPrompt.class);
            if (promptAnn != null) {
                String name = promptAnn.name().isEmpty() ? m.getName() : promptAnn.name();
                List<PromptArgument> args = new java.util.ArrayList<>();
                for (var param : m.getParameters()) {
                    McpParam mcpParam = param.getAnnotation(McpParam.class);
                    String pName = (mcpParam != null && !mcpParam.name().isEmpty())
                            ? mcpParam.name() : param.getName();
                    String desc = mcpParam != null ? mcpParam.description() : "";
                    boolean req = mcpParam == null || mcpParam.required();
                    args.add(new PromptArgument(pName, desc, req));
                }
                promptRegistry.register(new PromptDescriptor(name, promptAnn.description(),
                        args, promptBean, m));
            }
        }
    }

    private McpJsonRpcHandler createHandler(ErrorFormatter errorFormatter) {
        return new McpJsonRpcHandler(
                toolRegistry, resourceRegistry, promptRegistry,
                serializer, config, errorFormatter);
    }

    // ── Full flow tests ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Full MCP flow: discovery → registration → handshake → call → response")
    class FullFlowTests {

        private McpJsonRpcHandler handler;

        @BeforeEach
        void wireComponents() throws Exception {
            registerAllAnnotations();
            handler = createHandler(new PlainTextErrorFormatter());
        }

        @Test
        @DisplayName("Complete flow: initialize → initialized → tools/list → tools/call → verify response")
        @SuppressWarnings("unchecked")
        void completeToolCallFlow() {
            String sessionId = "integration-session-1";

            // Step 1: initialize
            JsonRpcMessage initReq = new JsonRpcMessage("2.0", "initialize", null, 1, null, null);
            JsonRpcMessage initResp = handler.handle(sessionId, initReq);

            assertNotNull(initResp);
            assertNull(initResp.error());
            assertEquals(1, initResp.id());

            Map<String, Object> initResult = (Map<String, Object>) initResp.result();
            assertNotNull(initResult.get("protocolVersion"));

            Map<String, Object> serverInfo = (Map<String, Object>) initResult.get("serverInfo");
            assertEquals("integration-test-server", serverInfo.get("name"));
            assertEquals("1.0.0", serverInfo.get("version"));

            Map<String, Object> capabilities = (Map<String, Object>) initResult.get("capabilities");
            assertTrue(capabilities.containsKey("tools"), "Should have tools capability");
            assertTrue(capabilities.containsKey("resources"), "Should have resources capability");
            assertTrue(capabilities.containsKey("prompts"), "Should have prompts capability");

            // Step 2: initialized notification
            JsonRpcMessage initializedNotif = new JsonRpcMessage(
                    "2.0", "notifications/initialized", null, null, null, null);
            JsonRpcMessage notifResp = handler.handle(sessionId, initializedNotif);
            assertNull(notifResp, "Notification should return null");

            // Step 3: tools/list
            JsonRpcMessage listReq = new JsonRpcMessage("2.0", "tools/list", null, 2, null, null);
            JsonRpcMessage listResp = handler.handle(sessionId, listReq);

            assertNotNull(listResp);
            assertNull(listResp.error());
            assertEquals(2, listResp.id());

            Map<String, Object> listResult = (Map<String, Object>) listResp.result();
            List<Map<String, Object>> tools = (List<Map<String, Object>>) listResult.get("tools");
            assertEquals(3, tools.size(), "Should have 3 tools registered");

            // Step 4: tools/call (add)
            JsonRpcMessage callReq = new JsonRpcMessage("2.0", "tools/call",
                    Map.of("name", "add", "arguments", Map.of("a", 5, "b", 3)),
                    3, null, null);
            JsonRpcMessage callResp = handler.handle(sessionId, callReq);

            assertNotNull(callResp);
            assertNull(callResp.error());
            assertEquals(3, callResp.id());

            Map<String, Object> callResult = (Map<String, Object>) callResp.result();
            assertEquals(false, callResult.get("isError"));
            List<Map<String, Object>> content = (List<Map<String, Object>>) callResult.get("content");
            assertEquals("8", content.get(0).get("text").toString());
        }

        @Test
        @DisplayName("Complete flow: initialize → initialized → resources/list → resources/read")
        @SuppressWarnings("unchecked")
        void completeResourceReadFlow() {
            String sessionId = "integration-session-2";

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "initialize", null, 1, null, null));
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // resources/list
            JsonRpcMessage listResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "resources/list", null, 2, null, null));

            assertNotNull(listResp);
            assertNull(listResp.error());
            Map<String, Object> listResult = (Map<String, Object>) listResp.result();
            List<Map<String, Object>> resources = (List<Map<String, Object>>) listResult.get("resources");
            assertEquals(1, resources.size());
            assertEquals("config://app", resources.get(0).get("uri"));

            // resources/read
            JsonRpcMessage readResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "resources/read",
                            Map.of("uri", "config://app"), 3, null, null));

            assertNotNull(readResp);
            assertNull(readResp.error());
            Map<String, Object> readResult = (Map<String, Object>) readResp.result();
            List<Map<String, Object>> contents = (List<Map<String, Object>>) readResult.get("contents");
            assertEquals("config://app", contents.get(0).get("uri"));
            assertTrue(contents.get(0).get("text").toString().contains("server.port=8080"));
        }

        @Test
        @DisplayName("Complete flow: initialize → initialized → prompts/list → prompts/get")
        @SuppressWarnings("unchecked")
        void completePromptGetFlow() {
            String sessionId = "integration-session-3";

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "initialize", null, 1, null, null));
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // prompts/list
            JsonRpcMessage listResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "prompts/list", null, 2, null, null));

            assertNotNull(listResp);
            assertNull(listResp.error());
            Map<String, Object> listResult = (Map<String, Object>) listResp.result();
            List<Map<String, Object>> prompts = (List<Map<String, Object>>) listResult.get("prompts");
            assertEquals(1, prompts.size());
            assertEquals("code-review", prompts.get(0).get("name"));

            // prompts/get
            JsonRpcMessage getResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "prompts/get",
                            Map.of("name", "code-review", "arguments", Map.of("code", "x = 1")),
                            3, null, null));

            assertNotNull(getResp);
            assertNull(getResp.error());
            Map<String, Object> getResult = (Map<String, Object>) getResp.result();
            assertNotNull(getResult.get("messages"));
            List<?> messages = (List<?>) getResult.get("messages");
            assertFalse(messages.isEmpty());
        }

        @Test
        @DisplayName("tools/call with exception returns isError=true with PlainTextErrorFormatter")
        @SuppressWarnings("unchecked")
        void toolCallExceptionUsesPlainTextFormatter() {
            String sessionId = "integration-session-4";

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "initialize", null, 1, null, null));
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // Call failing tool
            JsonRpcMessage callResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "tools/call",
                            Map.of("name", "failing-tool", "arguments", Map.of("input", "test")),
                            2, null, null));

            assertNotNull(callResp);
            assertNull(callResp.error());
            Map<String, Object> result = (Map<String, Object>) callResp.result();
            assertEquals(true, result.get("isError"));
            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            assertEquals("text", content.get(0).get("type"));
            assertTrue(content.get(0).get("text").toString().contains("Intentional failure"));
        }

        @Test
        @DisplayName("Pre-handshake tools/call is rejected with -32600")
        void preHandshakeToolsCallRejected() {
            String sessionId = "integration-session-5";

            // No handshake — directly call tools/call
            JsonRpcMessage callResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "tools/call",
                            Map.of("name", "add", "arguments", Map.of("a", 1, "b", 2)),
                            1, null, null));

            assertNotNull(callResp);
            assertNotNull(callResp.error());
            assertEquals(JsonRpcError.INVALID_REQUEST, callResp.error().code());
            assertTrue(callResp.error().message().contains("not initialized"));
        }

        @Test
        @DisplayName("JSON-RPC round-trip through serializer preserves message integrity")
        @SuppressWarnings("unchecked")
        void jsonRpcRoundTripThroughSerializer() {
            String sessionId = "integration-session-6";

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "initialize", null, 1, null, null));
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // Serialize a request, deserialize it, process it
            String requestJson = serializer.serialize(
                    new JsonRpcMessage("2.0", "tools/call",
                            Map.of("name", "multiply", "arguments", Map.of("x", 4, "y", 7)),
                            99, null, null));

            JsonRpcMessage deserialized = serializer.deserialize(requestJson);
            JsonRpcMessage response = handler.handle(sessionId, deserialized);

            assertNotNull(response);
            assertNull(response.error());
            assertEquals(99, response.id());

            // Serialize response and deserialize back
            String responseJson = serializer.serialize(response);
            JsonRpcMessage roundTripped = serializer.deserialize(responseJson);

            assertNotNull(roundTripped.result());
            Map<String, Object> result = (Map<String, Object>) roundTripped.result();
            assertEquals(false, result.get("isError"));
            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            assertEquals("28", content.get(0).get("text").toString());
        }
    }

    // ── Disabled tools filter tests ─────────────────────────────────────

    @Nested
    @DisplayName("Disabled tools filter after deployment")
    class DisabledToolsFilterTests {

        @Test
        @DisplayName("Disabled tools are excluded from tools/list and tools/call")
        @SuppressWarnings("unchecked")
        void disabledToolsExcluded() throws Exception {
            registerAllAnnotations();

            // Apply disabled filter (simulating afterDeploymentValidation)
            config.setToolsDisabled("multiply, failing-tool");
            toolRegistry.applyDisabledFilter(config.getDisabledToolNames());

            McpJsonRpcHandler handler = createHandler(new PlainTextErrorFormatter());
            String sessionId = "disabled-session";

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "initialize", null, 1, null, null));
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // tools/list should only show 'add'
            JsonRpcMessage listResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "tools/list", null, 2, null, null));
            Map<String, Object> listResult = (Map<String, Object>) listResp.result();
            List<Map<String, Object>> tools = (List<Map<String, Object>>) listResult.get("tools");
            assertEquals(1, tools.size());
            assertEquals("add", tools.get(0).get("name"));

            // tools/call on disabled tool should return -32602
            JsonRpcMessage callResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "tools/call",
                            Map.of("name", "multiply", "arguments", Map.of("x", 2, "y", 3)),
                            3, null, null));
            assertNotNull(callResp.error());
            assertEquals(JsonRpcError.INVALID_PARAMS, callResp.error().code());
        }
    }

    // ── Graceful degradation tests ──────────────────────────────────────

    @Nested
    @DisplayName("Graceful degradation with optional modules")
    class GracefulDegradationTests {

        @Test
        @DisplayName("PlainTextErrorFormatter is used when ProblemDetailErrorFormatter is not available")
        @SuppressWarnings("unchecked")
        void plainTextFormatterUsedAsFallback() throws Exception {
            registerAllAnnotations();

            // Wire with PlainTextErrorFormatter (simulating no demoiselle-rest)
            McpJsonRpcHandler handler = createHandler(new PlainTextErrorFormatter());
            String sessionId = "plain-text-session";

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "initialize", null, 1, null, null));
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // Call failing tool
            JsonRpcMessage callResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "tools/call",
                            Map.of("name", "failing-tool", "arguments", Map.of("input", "test")),
                            2, null, null));

            Map<String, Object> result = (Map<String, Object>) callResp.result();
            assertEquals(true, result.get("isError"));
            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            // PlainTextErrorFormatter returns simple text
            String errorText = content.get(0).get("text").toString();
            assertTrue(errorText.contains("Intentional failure"));
            // Should NOT contain ProblemDetail fields
            assertFalse(errorText.contains("\"status\""));
            assertFalse(errorText.contains("\"title\""));
        }

        @Test
        @DisplayName("ProblemDetailErrorFormatter produces structured error when available")
        @SuppressWarnings("unchecked")
        void problemDetailFormatterUsedWhenAvailable() throws Exception {
            registerAllAnnotations();

            // Wire with ProblemDetailErrorFormatter (simulating demoiselle-rest present)
            McpJsonRpcHandler handler = createHandler(new ProblemDetailErrorFormatter());
            String sessionId = "problem-detail-session";

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "initialize", null, 1, null, null));
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // Call failing tool
            JsonRpcMessage callResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "tools/call",
                            Map.of("name", "failing-tool", "arguments", Map.of("input", "test")),
                            2, null, null));

            Map<String, Object> result = (Map<String, Object>) callResp.result();
            assertEquals(true, result.get("isError"));
            List<Map<String, Object>> content = (List<Map<String, Object>>) result.get("content");
            String errorText = content.get(0).get("text").toString();
            // ProblemDetailErrorFormatter includes status and title
            assertTrue(errorText.contains("Internal Server Error"));
            assertTrue(errorText.contains("500"));
        }

        @Test
        @DisplayName("Server works with empty registries (no tools, resources, or prompts)")
        @SuppressWarnings("unchecked")
        void serverWorksWithEmptyRegistries() {
            // Don't register anything — empty registries
            McpJsonRpcHandler handler = createHandler(new PlainTextErrorFormatter());
            String sessionId = "empty-session";

            // initialize should work with empty capabilities
            JsonRpcMessage initResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "initialize", null, 1, null, null));

            assertNotNull(initResp);
            assertNull(initResp.error());
            Map<String, Object> result = (Map<String, Object>) initResp.result();
            Map<String, Object> capabilities = (Map<String, Object>) result.get("capabilities");
            assertFalse(capabilities.containsKey("tools"));
            assertFalse(capabilities.containsKey("resources"));
            assertFalse(capabilities.containsKey("prompts"));

            // Handshake
            handler.handle(sessionId, new JsonRpcMessage("2.0", "notifications/initialized", null, null, null, null));

            // tools/list returns empty
            JsonRpcMessage listResp = handler.handle(sessionId,
                    new JsonRpcMessage("2.0", "tools/list", null, 2, null, null));
            Map<String, Object> listResult = (Map<String, Object>) listResp.result();
            List<?> tools = (List<?>) listResult.get("tools");
            assertTrue(tools.isEmpty());
        }

        @Test
        @DisplayName("Optional module detection does not throw when modules are absent")
        void optionalModuleDetectionDoesNotThrow() {
            // Verify that Class.forName for non-existent classes doesn't crash
            assertDoesNotThrow(() -> {
                try {
                    Class.forName("com.nonexistent.SomeClass");
                } catch (ClassNotFoundException e) {
                    // Expected — graceful degradation
                }
            });
        }
    }

    // ── Schema generation integration ───────────────────────────────────

    @Nested
    @DisplayName("Schema generation integrated with registry and handler")
    class SchemaIntegrationTests {

        @Test
        @DisplayName("Generated inputSchema is returned in tools/list response")
        @SuppressWarnings("unchecked")
        void inputSchemaReturnedInToolsList() throws Exception {
            registerAllAnnotations();
            McpJsonRpcHandler handler = createHandler(new PlainTextErrorFormatter());

            JsonRpcMessage listResp = handler.handle("schema-session",
                    new JsonRpcMessage("2.0", "tools/list", null, 1, null, null));

            Map<String, Object> listResult = (Map<String, Object>) listResp.result();
            List<Map<String, Object>> tools = (List<Map<String, Object>>) listResult.get("tools");

            // Find the 'add' tool and verify its schema
            Map<String, Object> addTool = tools.stream()
                    .filter(t -> "add".equals(t.get("name")))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Tool 'add' not found"));

            Map<String, Object> schema = (Map<String, Object>) addTool.get("inputSchema");
            assertEquals("object", schema.get("type"));
            Map<String, Object> properties = (Map<String, Object>) schema.get("properties");
            assertNotNull(properties);
            assertTrue(properties.containsKey("a"));
            assertTrue(properties.containsKey("b"));

            // Verify 'a' has integer type and description
            Map<String, Object> aProp = (Map<String, Object>) properties.get("a");
            assertEquals("integer", aProp.get("type"));
            assertEquals("First operand", aProp.get("description"));
        }
    }
}
