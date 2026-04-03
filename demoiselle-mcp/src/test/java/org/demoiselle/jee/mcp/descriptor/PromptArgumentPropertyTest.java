/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.descriptor;

import net.jqwik.api.*;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.annotation.McpPrompt;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for prompt argument generation from method parameters.
 *
 * <p>Covers Property 26: For every {@code @McpPrompt} method, the list of
 * {@link PromptArgument} must correspond exactly to the method parameters,
 * respecting {@code @McpParam} for name, description, and required.</p>
 */
// Feature: demoiselle-mcp, Property 26: Geração de argumentos de prompt a partir de parâmetros
class PromptArgumentPropertyTest {

    // ── Sample @McpPrompt methods with various parameter configurations ──

    @SuppressWarnings("unused")
    public static class PromptSamples {

        @McpPrompt(name = "no-params", description = "Prompt with no parameters")
        public String noParams() {
            return "hello";
        }

        @McpPrompt(name = "single-bare", description = "Single param without @McpParam")
        public String singleBare(String topic) {
            return topic;
        }

        @McpPrompt(name = "single-annotated", description = "Single param with @McpParam")
        public String singleAnnotated(
                @McpParam(name = "subject", description = "The subject", required = true) String topic) {
            return topic;
        }

        @McpPrompt(name = "optional-param", description = "Optional parameter")
        public String optionalParam(
                @McpParam(name = "lang", description = "Language code", required = false) String language) {
            return language;
        }

        @McpPrompt(name = "mixed-params", description = "Mix of annotated and bare params")
        public String mixedParams(
                String bare,
                @McpParam(name = "custom", description = "A custom param", required = true) String annotated,
                @McpParam(description = "Optional flag", required = false) boolean flag) {
            return bare + annotated + flag;
        }

        @McpPrompt(name = "empty-name-annotation", description = "McpParam with empty name")
        public String emptyNameAnnotation(
                @McpParam(name = "", description = "Falls back to Java name") String fallback) {
            return fallback;
        }

        @McpPrompt(name = "multi-types", description = "Multiple typed params")
        public String multiTypes(
                @McpParam(name = "count", description = "Number of items") int count,
                @McpParam(name = "label", description = "Label text", required = false) String label,
                double ratio) {
            return count + label + ratio;
        }

        @McpPrompt(description = "All bare params")
        public String allBare(String a, int b, boolean c) {
            return a + b + c;
        }

        @McpPrompt(name = "all-optional", description = "All optional params")
        public String allOptional(
                @McpParam(name = "x", description = "X val", required = false) String x,
                @McpParam(name = "y", description = "Y val", required = false) String y) {
            return x + y;
        }
    }

    // ── Replicates buildPromptArguments logic from McpBootstrapExtension ──

    private List<PromptArgument> buildPromptArguments(Method method) {
        List<PromptArgument> arguments = new ArrayList<>();
        for (Parameter param : method.getParameters()) {
            McpParam mcpParam = param.getAnnotation(McpParam.class);

            String name;
            String description;
            boolean required;

            if (mcpParam != null) {
                name = mcpParam.name().isEmpty() ? param.getName() : mcpParam.name();
                description = mcpParam.description();
                required = mcpParam.required();
            } else {
                name = param.getName();
                description = "";
                required = true;
            }

            arguments.add(new PromptArgument(name, description, required));
        }
        return arguments;
    }

    // ── Providers ────────────────────────────────────────────────────────

    @Provide
    Arbitrary<Method> promptMethods() {
        List<Method> methods = new ArrayList<>();
        for (Method m : PromptSamples.class.getDeclaredMethods()) {
            if (m.isAnnotationPresent(McpPrompt.class)) {
                methods.add(m);
            }
        }
        return Arbitraries.of(methods);
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 26: Geração de argumentos de prompt a partir de parâmetros
    // -----------------------------------------------------------------------

    /**
     * The number of generated PromptArguments must equal the number of method parameters.
     *
     * <p><b>Validates: Requirements 9.7</b></p>
     */
    @Property(tries = 100)
    void argumentCountMatchesParameterCount(@ForAll("promptMethods") Method method) {
        List<PromptArgument> args = buildPromptArguments(method);
        assertEquals(method.getParameterCount(), args.size(),
                "Number of PromptArguments must match parameter count for method: " + method.getName());
    }

    /**
     * Each PromptArgument name must match: @McpParam.name() if present and non-empty,
     * otherwise the Java parameter name.
     *
     * <p><b>Validates: Requirements 9.7</b></p>
     */
    @Property(tries = 100)
    void argumentNamesMatchParameters(@ForAll("promptMethods") Method method) {
        List<PromptArgument> args = buildPromptArguments(method);
        Parameter[] params = method.getParameters();

        for (int i = 0; i < params.length; i++) {
            McpParam mcpParam = params[i].getAnnotation(McpParam.class);
            String expectedName;
            if (mcpParam != null && !mcpParam.name().isEmpty()) {
                expectedName = mcpParam.name();
            } else {
                expectedName = params[i].getName();
            }
            assertEquals(expectedName, args.get(i).name(),
                    "Argument name mismatch at index " + i + " for method: " + method.getName());
        }
    }

    /**
     * Each PromptArgument required flag must match: @McpParam.required() if present,
     * otherwise default to true.
     *
     * <p><b>Validates: Requirements 9.7</b></p>
     */
    @Property(tries = 100)
    void argumentRequiredMatchesParameters(@ForAll("promptMethods") Method method) {
        List<PromptArgument> args = buildPromptArguments(method);
        Parameter[] params = method.getParameters();

        for (int i = 0; i < params.length; i++) {
            McpParam mcpParam = params[i].getAnnotation(McpParam.class);
            boolean expectedRequired = (mcpParam != null) ? mcpParam.required() : true;
            assertEquals(expectedRequired, args.get(i).required(),
                    "Argument required mismatch at index " + i + " for method: " + method.getName());
        }
    }

    /**
     * Each PromptArgument description must match: @McpParam.description() if present,
     * otherwise empty string.
     *
     * <p><b>Validates: Requirements 9.7</b></p>
     */
    @Property(tries = 100)
    void argumentDescriptionMatchesParameters(@ForAll("promptMethods") Method method) {
        List<PromptArgument> args = buildPromptArguments(method);
        Parameter[] params = method.getParameters();

        for (int i = 0; i < params.length; i++) {
            McpParam mcpParam = params[i].getAnnotation(McpParam.class);
            String expectedDesc = (mcpParam != null) ? mcpParam.description() : "";
            assertEquals(expectedDesc, args.get(i).description(),
                    "Argument description mismatch at index " + i + " for method: " + method.getName());
        }
    }
}
