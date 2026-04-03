/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.schema;

import net.jqwik.api.*;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.annotation.McpTool;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link JsonSchemaGenerator}.
 *
 * <p>Covers Property 1 (valid schema invariant), Property 2 (Java→JSON Schema type mapping),
 * and Property 3 (parameter metadata: required and description).</p>
 */
class JsonSchemaGeneratorPropertyTest {

    private final JsonSchemaGenerator generator = new JsonSchemaGenerator();

    // -----------------------------------------------------------------------
    // Helper POJO for testing object mapping
    // -----------------------------------------------------------------------

    @SuppressWarnings("unused")
    static class SamplePojo {
        String label;
        int value;
    }

    // -----------------------------------------------------------------------
    // Sample tool methods with various parameter combinations (Property 1)
    // -----------------------------------------------------------------------

    @SuppressWarnings("unused")
    static class SchemaToolSamples {

        @McpTool(description = "no params")
        public void noParams() {}

        @McpTool(description = "single string")
        public void singleString(@McpParam(name = "s") String s) {}

        @McpTool(description = "single int")
        public void singleInt(@McpParam(name = "n") int n) {}

        @McpTool(description = "single boolean")
        public void singleBoolean(@McpParam(name = "b") boolean b) {}

        @McpTool(description = "multiple primitives")
        public void multiplePrimitives(
                @McpParam(name = "a") String a,
                @McpParam(name = "b") int b,
                @McpParam(name = "c") double c,
                @McpParam(name = "d") boolean d) {}

        @McpTool(description = "with list")
        public void withList(@McpParam(name = "items") List<String> items) {}

        @McpTool(description = "with array")
        public void withArray(@McpParam(name = "arr") int[] arr) {}

        @McpTool(description = "with pojo")
        public void withPojo(@McpParam(name = "obj") SamplePojo obj) {}

        @McpTool(description = "wrappers")
        public void wrappers(
                @McpParam(name = "i") Integer i,
                @McpParam(name = "l") Long l,
                @McpParam(name = "d") Double d,
                @McpParam(name = "f") Float f,
                @McpParam(name = "b") Boolean b) {}

        @McpTool(description = "mixed types")
        public void mixedTypes(
                @McpParam(name = "name") String name,
                @McpParam(name = "tags") List<String> tags,
                @McpParam(name = "scores") double[] scores,
                @McpParam(name = "meta") SamplePojo meta) {}
    }

    // -----------------------------------------------------------------------
    // Sample methods for type mapping (Property 2)
    // -----------------------------------------------------------------------

    /** Enum representing supported Java types and their expected JSON Schema type. */
    enum JavaTypeMapping {
        STRING(String.class, "string"),
        INT(int.class, "integer"),
        INTEGER(Integer.class, "integer"),
        LONG_PRIM(long.class, "integer"),
        LONG_WRAP(Long.class, "integer"),
        DOUBLE_PRIM(double.class, "number"),
        DOUBLE_WRAP(Double.class, "number"),
        FLOAT_PRIM(float.class, "number"),
        FLOAT_WRAP(Float.class, "number"),
        BOOLEAN_PRIM(boolean.class, "boolean"),
        BOOLEAN_WRAP(Boolean.class, "boolean");

        final Class<?> javaType;
        final String expectedJsonType;

        JavaTypeMapping(Class<?> javaType, String expectedJsonType) {
            this.javaType = javaType;
            this.expectedJsonType = expectedJsonType;
        }
    }

    /** Enum for collection/complex types that map to "array" or "object". */
    enum ComplexTypeMapping {
        LIST_STRING("withListString", "array"),
        ARRAY_INT("withArrayInt", "array"),
        POJO("withPojo", "object");

        final String methodName;
        final String expectedJsonType;

        ComplexTypeMapping(String methodName, String expectedJsonType) {
            this.methodName = methodName;
            this.expectedJsonType = expectedJsonType;
        }
    }

    @SuppressWarnings("unused")
    static class TypeMappingSamples {
        public void withListString(List<String> items) {}
        public void withArrayInt(int[] arr) {}
        public void withPojo(SamplePojo obj) {}
    }

    // -----------------------------------------------------------------------
    // Sample methods for parameter metadata (Property 3)
    // -----------------------------------------------------------------------

    @SuppressWarnings("unused")
    static class MetadataSamples {

        @McpTool(description = "no annotation - required by default")
        public void noAnnotation(String name) {}

        @McpTool(description = "explicit required true")
        public void explicitRequired(@McpParam(name = "x", required = true) String x) {}

        @McpTool(description = "explicit required false")
        public void explicitOptional(@McpParam(name = "x", required = false) String x) {}

        @McpTool(description = "with description")
        public void withDescription(
                @McpParam(name = "city", description = "The city name") String city) {}

        @McpTool(description = "mixed required and optional")
        public void mixedRequiredOptional(
                @McpParam(name = "req1") String req1,
                @McpParam(name = "req2", required = true) String req2,
                @McpParam(name = "opt1", required = false) String opt1) {}

        @McpTool(description = "all optional")
        public void allOptional(
                @McpParam(name = "a", required = false) String a,
                @McpParam(name = "b", required = false) int b) {}

        @McpTool(description = "descriptions on some")
        public void descriptionsOnSome(
                @McpParam(name = "p1", description = "First param") String p1,
                @McpParam(name = "p2") String p2,
                @McpParam(name = "p3", description = "Third param") int p3) {}

        @McpTool(description = "multiple no annotation")
        public void multipleNoAnnotation(String a, int b, boolean c) {}
    }

    // -----------------------------------------------------------------------
    // Arbitraries
    // -----------------------------------------------------------------------

    private static final Method[] SCHEMA_TOOL_METHODS;

    static {
        SCHEMA_TOOL_METHODS = Arrays.stream(SchemaToolSamples.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(McpTool.class))
                .toArray(Method[]::new);
    }

    @Provide
    Arbitrary<Method> schemaMethods() {
        return Arbitraries.of(SCHEMA_TOOL_METHODS);
    }

    @Provide
    Arbitrary<JavaTypeMapping> javaTypeMappings() {
        return Arbitraries.of(JavaTypeMapping.values());
    }

    @Provide
    Arbitrary<ComplexTypeMapping> complexTypeMappings() {
        return Arbitraries.of(ComplexTypeMapping.values());
    }

    private static final Method[] METADATA_METHODS;

    static {
        METADATA_METHODS = Arrays.stream(MetadataSamples.class.getDeclaredMethods())
                .filter(m -> m.isAnnotationPresent(McpTool.class))
                .toArray(Method[]::new);
    }

    @Provide
    Arbitrary<Method> metadataMethods() {
        return Arbitraries.of(METADATA_METHODS);
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 1: Invariante de schema válido
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 1: Invariante de schema válido
    /**
     * For every @McpTool method, the generated inputSchema must have
     * "type" equal to "object" and a non-null "properties" map.
     *
     * <p><b>Validates: Requirements 1.6, 16.1, 16.7</b></p>
     */
    @Property(tries = 100)
    @SuppressWarnings("unchecked")
    void generatedSchemaAlwaysHasTypeObjectAndNonNullProperties(
            @ForAll("schemaMethods") Method method) {

        Map<String, Object> schema = generator.generate(method);

        assertNotNull(schema, "Schema must not be null");
        assertEquals("object", schema.get("type"),
                "Schema 'type' must be 'object' for method: " + method.getName());

        Object properties = schema.get("properties");
        assertNotNull(properties,
                "Schema 'properties' must not be null for method: " + method.getName());
        assertInstanceOf(Map.class, properties,
                "Schema 'properties' must be a Map for method: " + method.getName());

        // Additional: number of properties must match number of parameters
        Map<String, Object> propsMap = (Map<String, Object>) properties;
        assertEquals(method.getParameterCount(), propsMap.size(),
                "Number of schema properties must match parameter count for method: " + method.getName());
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 2: Mapeamento correto de tipos Java → JSON Schema
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 2: Mapeamento correto de tipos Java → JSON Schema
    /**
     * For every primitive/wrapper type, mapType() must produce the corresponding
     * JSON Schema type string.
     *
     * <p><b>Validates: Requirements 1.3, 16.2</b></p>
     */
    @Property(tries = 100)
    void mapTypePrimitiveAndWrapperProducesCorrectJsonType(
            @ForAll("javaTypeMappings") JavaTypeMapping mapping) {

        Map<String, Object> result = generator.mapType(mapping.javaType, mapping.javaType);

        assertNotNull(result, "mapType result must not be null for " + mapping.javaType.getSimpleName());
        assertEquals(mapping.expectedJsonType, result.get("type"),
                "mapType(" + mapping.javaType.getSimpleName() + ") must produce type '"
                        + mapping.expectedJsonType + "'");
    }

    // Feature: demoiselle-mcp, Property 2: Mapeamento correto de tipos Java → JSON Schema
    /**
     * For List, array, and POJO types, mapType() must produce the correct
     * JSON Schema type ("array" with items, or "object" with properties).
     *
     * <p><b>Validates: Requirements 1.3, 16.3, 16.4</b></p>
     */
    @Property(tries = 100)
    @SuppressWarnings("unchecked")
    void mapTypeComplexTypesProducesCorrectJsonType(
            @ForAll("complexTypeMappings") ComplexTypeMapping mapping) throws Exception {

        Method method = TypeMappingSamples.class.getDeclaredMethod(
                mapping.methodName, getParamType(mapping));
        Class<?> paramType = method.getParameterTypes()[0];
        java.lang.reflect.Type genericType = method.getGenericParameterTypes()[0];

        Map<String, Object> result = generator.mapType(paramType, genericType);

        assertNotNull(result, "mapType result must not be null for " + mapping.methodName);
        assertEquals(mapping.expectedJsonType, result.get("type"),
                "mapType for " + mapping.methodName + " must produce type '" + mapping.expectedJsonType + "'");

        if ("array".equals(mapping.expectedJsonType)) {
            assertNotNull(result.get("items"),
                    "Array type must have 'items' for " + mapping.methodName);
        }
        if ("object".equals(mapping.expectedJsonType)) {
            assertNotNull(result.get("properties"),
                    "Object type must have 'properties' for " + mapping.methodName);
        }
    }

    private Class<?> getParamType(ComplexTypeMapping mapping) {
        return switch (mapping) {
            case LIST_STRING -> List.class;
            case ARRAY_INT -> int[].class;
            case POJO -> SamplePojo.class;
        };
    }

    // -----------------------------------------------------------------------
    // Feature: demoiselle-mcp, Property 3: Metadados de parâmetros (required e description)
    // -----------------------------------------------------------------------

    // Feature: demoiselle-mcp, Property 3: Metadados de parâmetros (required e description)
    /**
     * For every parameter without @McpParam or with @McpParam(required=true),
     * the parameter name must appear in the "required" array.
     * For parameters with @McpParam(required=false), the name must NOT appear in "required".
     *
     * <p><b>Validates: Requirements 1.5, 16.5</b></p>
     */
    @Property(tries = 100)
    @SuppressWarnings("unchecked")
    void requiredArrayReflectsAnnotationOrDefault(
            @ForAll("metadataMethods") Method method) {

        Map<String, Object> schema = generator.generate(method);
        List<String> required = (List<String>) schema.get("required");

        Parameter[] params = method.getParameters();
        for (Parameter param : params) {
            McpParam mcpParam = param.getAnnotation(McpParam.class);
            String paramName;
            if (mcpParam != null && !mcpParam.name().isEmpty()) {
                paramName = mcpParam.name();
            } else {
                paramName = param.getName();
            }

            boolean shouldBeRequired = (mcpParam == null || mcpParam.required());

            if (shouldBeRequired) {
                assertNotNull(required,
                        "required array must not be null when there are required params in method: "
                                + method.getName());
                assertTrue(required.contains(paramName),
                        "Parameter '" + paramName + "' should be in required array for method: "
                                + method.getName());
            } else {
                if (required != null) {
                    assertFalse(required.contains(paramName),
                            "Optional parameter '" + paramName
                                    + "' should NOT be in required array for method: "
                                    + method.getName());
                }
            }
        }
    }

    // Feature: demoiselle-mcp, Property 3: Metadados de parâmetros (required e description)
    /**
     * For every parameter with @McpParam(description="..."), the corresponding
     * property in the schema must contain the "description" field with the specified value.
     * For parameters without description, the field must be absent.
     *
     * <p><b>Validates: Requirements 1.5, 16.6</b></p>
     */
    @Property(tries = 100)
    @SuppressWarnings("unchecked")
    void descriptionFieldReflectsAnnotation(
            @ForAll("metadataMethods") Method method) {

        Map<String, Object> schema = generator.generate(method);
        Map<String, Map<String, Object>> properties =
                (Map<String, Map<String, Object>>) schema.get("properties");

        Parameter[] params = method.getParameters();
        for (Parameter param : params) {
            McpParam mcpParam = param.getAnnotation(McpParam.class);
            String paramName;
            if (mcpParam != null && !mcpParam.name().isEmpty()) {
                paramName = mcpParam.name();
            } else {
                paramName = param.getName();
            }

            Map<String, Object> propSchema = properties.get(paramName);
            assertNotNull(propSchema,
                    "Property '" + paramName + "' must exist in schema for method: " + method.getName());

            if (mcpParam != null && !mcpParam.description().isEmpty()) {
                assertEquals(mcpParam.description(), propSchema.get("description"),
                        "Description for '" + paramName + "' must match @McpParam.description in method: "
                                + method.getName());
            } else {
                assertNull(propSchema.get("description"),
                        "Parameter '" + paramName
                                + "' without description should not have 'description' field in method: "
                                + method.getName());
            }
        }
    }
}
