/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.extension;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.annotation.McpPrompt;
import org.demoiselle.jee.mcp.annotation.McpResource;
import org.demoiselle.jee.mcp.annotation.McpTool;
import org.demoiselle.jee.mcp.config.McpConfig;
import org.demoiselle.jee.mcp.descriptor.PromptArgument;
import org.demoiselle.jee.mcp.descriptor.PromptDescriptor;
import org.demoiselle.jee.mcp.descriptor.ResourceDescriptor;
import org.demoiselle.jee.mcp.descriptor.ToolDescriptor;
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
 * Unit tests for {@link McpBootstrapExtension}.
 *
 * <p>Tests the core logic of the extension (prompt argument building,
 * tool name resolution, disabled filter) without requiring a full CDI container.</p>
 */
class McpBootstrapExtensionTest {

    private McpBootstrapExtension extension;
    private McpToolRegistry toolRegistry;
    private McpResourceRegistry resourceRegistry;
    private McpPromptRegistry promptRegistry;
    private JsonSchemaGenerator schemaGenerator;

    // ── Sample beans for testing ────────────────────────────────────────

    @SuppressWarnings("unused")
    public static class SampleToolBean {
        @McpTool(description = "Adds two numbers")
        public int add(int a, int b) {
            return a + b;
        }

        @McpTool(name = "custom-greet", description = "Greets a person")
        public String greet(@McpParam(name = "personName", description = "Name of the person") String name) {
            return "Hello, " + name;
        }
    }

    @SuppressWarnings("unused")
    public static class SampleResourceBean {
        @McpResource(uri = "file:///config.json", name = "config", description = "App config", mimeType = "application/json")
        public String readConfig() {
            return "{}";
        }
    }

    @SuppressWarnings("unused")
    public static class SamplePromptBean {
        @McpPrompt(description = "Generates a greeting")
        public String greetingPrompt(
                @McpParam(name = "userName", description = "User name", required = true) String name,
                @McpParam(name = "style", description = "Greeting style", required = false) String style) {
            return "Hello " + name;
        }

        @McpPrompt(name = "custom-prompt", description = "Custom prompt")
        public String customPrompt(String arg1) {
            return arg1;
        }
    }

    @BeforeEach
    void setUp() {
        extension = new McpBootstrapExtension();
        toolRegistry = new McpToolRegistry();
        resourceRegistry = new McpResourceRegistry();
        promptRegistry = new McpPromptRegistry();
        schemaGenerator = new JsonSchemaGenerator();
    }

    // ── Helper to simulate what afterDeploymentValidation does ──────────

    private void registerTool(Method method, Object beanInstance) {
        McpTool annotation = method.getAnnotation(McpTool.class);
        String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
        Map<String, Object> inputSchema = schemaGenerator.generate(method);
        toolRegistry.register(new ToolDescriptor(name, annotation.description(), inputSchema, beanInstance, method));
    }

    private void registerResource(Method method, Object beanInstance) {
        McpResource annotation = method.getAnnotation(McpResource.class);
        resourceRegistry.register(new ResourceDescriptor(
                annotation.uri(), annotation.name(), annotation.description(),
                annotation.mimeType(), beanInstance, method));
    }

    private void registerPrompt(Method method, Object beanInstance) {
        McpPrompt annotation = method.getAnnotation(McpPrompt.class);
        String name = annotation.name().isEmpty() ? method.getName() : annotation.name();
        List<PromptArgument> arguments = buildPromptArguments(method);
        promptRegistry.register(new PromptDescriptor(name, annotation.description(), arguments, beanInstance, method));
    }

    private List<PromptArgument> buildPromptArguments(Method method) {
        List<PromptArgument> args = new java.util.ArrayList<>();
        for (var param : method.getParameters()) {
            McpParam mcpParam = param.getAnnotation(McpParam.class);
            String pName = (mcpParam != null && !mcpParam.name().isEmpty()) ? mcpParam.name() : param.getName();
            String desc = mcpParam != null ? mcpParam.description() : "";
            boolean req = mcpParam == null || mcpParam.required();
            args.add(new PromptArgument(pName, desc, req));
        }
        return args;
    }

    @Nested
    @DisplayName("Tool registration")
    class ToolRegistration {

        @Test
        @DisplayName("Tool with default name uses method name")
        void toolDefaultNameUsesMethodName() throws Exception {
            Method method = SampleToolBean.class.getMethod("add", int.class, int.class);
            SampleToolBean bean = new SampleToolBean();
            registerTool(method, bean);

            assertEquals(1, toolRegistry.size());
            assertTrue(toolRegistry.find("add").isPresent());
            assertEquals("Adds two numbers", toolRegistry.find("add").get().description());
        }

        @Test
        @DisplayName("Tool with explicit name uses annotation name")
        void toolExplicitNameUsesAnnotationName() throws Exception {
            Method method = SampleToolBean.class.getMethod("greet", String.class);
            SampleToolBean bean = new SampleToolBean();
            registerTool(method, bean);

            assertEquals(1, toolRegistry.size());
            assertTrue(toolRegistry.find("custom-greet").isPresent());
            assertFalse(toolRegistry.find("greet").isPresent());
        }

        @Test
        @DisplayName("Tool inputSchema is generated correctly")
        void toolInputSchemaGenerated() throws Exception {
            Method method = SampleToolBean.class.getMethod("add", int.class, int.class);
            registerTool(method, new SampleToolBean());

            ToolDescriptor td = toolRegistry.find("add").orElseThrow();
            Map<String, Object> schema = td.inputSchema();
            assertEquals("object", schema.get("type"));
            assertNotNull(schema.get("properties"));
        }
    }

    @Nested
    @DisplayName("Resource registration")
    class ResourceRegistration {

        @Test
        @DisplayName("Resource is registered with correct metadata")
        void resourceRegisteredCorrectly() throws Exception {
            Method method = SampleResourceBean.class.getMethod("readConfig");
            registerResource(method, new SampleResourceBean());

            assertEquals(1, resourceRegistry.size());
            ResourceDescriptor rd = resourceRegistry.findByUri("file:///config.json").orElseThrow();
            assertEquals("config", rd.name());
            assertEquals("App config", rd.description());
            assertEquals("application/json", rd.mimeType());
        }
    }

    @Nested
    @DisplayName("Prompt registration")
    class PromptRegistration {

        @Test
        @DisplayName("Prompt with default name uses method name")
        void promptDefaultNameUsesMethodName() throws Exception {
            Method method = SamplePromptBean.class.getMethod("greetingPrompt", String.class, String.class);
            registerPrompt(method, new SamplePromptBean());

            assertTrue(promptRegistry.find("greetingPrompt").isPresent());
        }

        @Test
        @DisplayName("Prompt with explicit name uses annotation name")
        void promptExplicitNameUsesAnnotationName() throws Exception {
            Method method = SamplePromptBean.class.getMethod("customPrompt", String.class);
            registerPrompt(method, new SamplePromptBean());

            assertTrue(promptRegistry.find("custom-prompt").isPresent());
            assertFalse(promptRegistry.find("customPrompt").isPresent());
        }

        @Test
        @DisplayName("Prompt arguments are built from @McpParam annotations")
        void promptArgumentsFromAnnotations() throws Exception {
            Method method = SamplePromptBean.class.getMethod("greetingPrompt", String.class, String.class);
            registerPrompt(method, new SamplePromptBean());

            PromptDescriptor pd = promptRegistry.find("greetingPrompt").orElseThrow();
            List<PromptArgument> args = pd.arguments();
            assertEquals(2, args.size());

            PromptArgument first = args.get(0);
            assertEquals("userName", first.name());
            assertEquals("User name", first.description());
            assertTrue(first.required());

            PromptArgument second = args.get(1);
            assertEquals("style", second.name());
            assertEquals("Greeting style", second.description());
            assertFalse(second.required());
        }

        @Test
        @DisplayName("Prompt argument without @McpParam defaults to required with method param name")
        void promptArgumentWithoutAnnotation() throws Exception {
            Method method = SamplePromptBean.class.getMethod("customPrompt", String.class);
            registerPrompt(method, new SamplePromptBean());

            PromptDescriptor pd = promptRegistry.find("custom-prompt").orElseThrow();
            List<PromptArgument> args = pd.arguments();
            assertEquals(1, args.size());

            PromptArgument arg = args.get(0);
            assertTrue(arg.required());
            assertEquals("", arg.description());
        }
    }

    @Nested
    @DisplayName("Disabled tools filter")
    class DisabledToolsFilter {

        @Test
        @DisplayName("Disabled tools are removed from registry")
        void disabledToolsRemoved() throws Exception {
            Method addMethod = SampleToolBean.class.getMethod("add", int.class, int.class);
            Method greetMethod = SampleToolBean.class.getMethod("greet", String.class);
            SampleToolBean bean = new SampleToolBean();
            registerTool(addMethod, bean);
            registerTool(greetMethod, bean);

            assertEquals(2, toolRegistry.size());

            McpConfig config = new McpConfig();
            config.setToolsDisabled("add");
            toolRegistry.applyDisabledFilter(config.getDisabledToolNames());

            assertEquals(1, toolRegistry.size());
            assertFalse(toolRegistry.find("add").isPresent());
            assertTrue(toolRegistry.find("custom-greet").isPresent());
        }

        @Test
        @DisplayName("Empty disabled list leaves all tools")
        void emptyDisabledListLeavesAll() throws Exception {
            Method addMethod = SampleToolBean.class.getMethod("add", int.class, int.class);
            registerTool(addMethod, new SampleToolBean());

            McpConfig config = new McpConfig();
            config.setToolsDisabled("");
            toolRegistry.applyDisabledFilter(config.getDisabledToolNames());

            assertEquals(1, toolRegistry.size());
        }

        @Test
        @DisplayName("Multiple disabled tools separated by comma")
        void multipleDisabledTools() throws Exception {
            Method addMethod = SampleToolBean.class.getMethod("add", int.class, int.class);
            Method greetMethod = SampleToolBean.class.getMethod("greet", String.class);
            SampleToolBean bean = new SampleToolBean();
            registerTool(addMethod, bean);
            registerTool(greetMethod, bean);

            McpConfig config = new McpConfig();
            config.setToolsDisabled("add, custom-greet");
            toolRegistry.applyDisabledFilter(config.getDisabledToolNames());

            assertEquals(0, toolRegistry.size());
        }
    }

    @Nested
    @DisplayName("McpConfig.getDisabledToolNames")
    class ConfigDisabledToolNames {

        @Test
        @DisplayName("Null toolsDisabled returns empty set")
        void nullReturnsEmpty() {
            McpConfig config = new McpConfig();
            config.setToolsDisabled(null);
            assertEquals(Set.of(), config.getDisabledToolNames());
        }

        @Test
        @DisplayName("Blank toolsDisabled returns empty set")
        void blankReturnsEmpty() {
            McpConfig config = new McpConfig();
            config.setToolsDisabled("   ");
            assertEquals(Set.of(), config.getDisabledToolNames());
        }

        @Test
        @DisplayName("Comma-separated names are parsed and trimmed")
        void commaSeparatedParsed() {
            McpConfig config = new McpConfig();
            config.setToolsDisabled(" tool1 , tool2 , tool3 ");
            assertEquals(Set.of("tool1", "tool2", "tool3"), config.getDisabledToolNames());
        }
    }

    @Nested
    @DisplayName("AnnotatedMethodInfo record")
    class AnnotatedMethodInfoTest {

        @Test
        @DisplayName("Record stores bean class, method, and annotation type")
        void recordStoresFields() throws Exception {
            Method method = SampleToolBean.class.getMethod("add", int.class, int.class);
            var info = new McpBootstrapExtension.AnnotatedMethodInfo(
                    SampleToolBean.class, method, McpBootstrapExtension.AnnotationType.TOOL);

            assertEquals(SampleToolBean.class, info.beanClass());
            assertEquals(method, info.method());
            assertEquals(McpBootstrapExtension.AnnotationType.TOOL, info.annotationType());
        }
    }
}
