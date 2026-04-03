/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.schema;

import org.demoiselle.jee.mcp.annotation.McpParam;
import org.demoiselle.jee.mcp.annotation.McpTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para {@link JsonSchemaGenerator}.
 */
class JsonSchemaGeneratorTest {

    private JsonSchemaGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new JsonSchemaGenerator();
    }

    // ---- Sample classes for testing ----

    @SuppressWarnings("unused")
    static class SampleTools {

        @McpTool(description = "no params")
        public void noParams() {}

        @McpTool(description = "primitive types")
        public void primitiveTypes(
                @McpParam(name = "name") String name,
                @McpParam(name = "age") int age,
                @McpParam(name = "count") long count,
                @McpParam(name = "score") double score,
                @McpParam(name = "ratio") float ratio,
                @McpParam(name = "active") boolean active) {}

        @McpTool(description = "wrapper types")
        public void wrapperTypes(
                @McpParam(name = "age") Integer age,
                @McpParam(name = "count") Long count,
                @McpParam(name = "score") Double score,
                @McpParam(name = "ratio") Float ratio,
                @McpParam(name = "active") Boolean active) {}

        @McpTool(description = "with list")
        public void withList(@McpParam(name = "tags") List<String> tags) {}

        @McpTool(description = "with array")
        public void withArray(@McpParam(name = "items") String[] items) {}

        @McpTool(description = "with pojo")
        public void withPojo(@McpParam(name = "address") Address address) {}

        @McpTool(description = "with mcpparam")
        public void withMcpParam(
                @McpParam(name = "user_name", description = "The user name", required = true) String name,
                @McpParam(name = "age", required = false, description = "Optional age") int age) {}

        @McpTool(description = "no annotation means required")
        public void noAnnotation(String name) {}

        @McpTool(description = "custom name via mcpparam")
        public void customName(@McpParam(name = "custom") String original) {}

        @McpTool(description = "mixed params")
        public void mixedParams(
                @McpParam(name = "required1") String required1,
                @McpParam(name = "required2", required = true) String required2,
                @McpParam(name = "optional1", required = false) String optional1) {}

        @McpTool(description = "only optional params")
        public void onlyOptional(
                @McpParam(name = "a", required = false) String a,
                @McpParam(name = "b", required = false) String b) {}
    }

    static class Address {
        String street;
        int number;
    }

    // ---- Tests ----

    @Test
    void generate_noParams_returnsObjectSchemaWithEmptyProperties() throws Exception {
        Method m = SampleTools.class.getMethod("noParams");
        Map<String, Object> schema = generator.generate(m);

        assertEquals("object", schema.get("type"));
        assertNotNull(schema.get("properties"));
        assertTrue(((Map<?, ?>) schema.get("properties")).isEmpty());
        assertNull(schema.get("required"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_primitiveTypes_mapsCorrectly() throws Exception {
        Method m = SampleTools.class.getMethod("primitiveTypes",
                String.class, int.class, long.class, double.class, float.class, boolean.class);
        Map<String, Object> schema = generator.generate(m);

        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");

        assertEquals("string", props.get("name").get("type"));
        assertEquals("integer", props.get("age").get("type"));
        assertEquals("integer", props.get("count").get("type"));
        assertEquals("number", props.get("score").get("type"));
        assertEquals("number", props.get("ratio").get("type"));
        assertEquals("boolean", props.get("active").get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_wrapperTypes_mapsCorrectly() throws Exception {
        Method m = SampleTools.class.getMethod("wrapperTypes",
                Integer.class, Long.class, Double.class, Float.class, Boolean.class);
        Map<String, Object> schema = generator.generate(m);

        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");

        assertEquals("integer", props.get("age").get("type"));
        assertEquals("integer", props.get("count").get("type"));
        assertEquals("number", props.get("score").get("type"));
        assertEquals("number", props.get("ratio").get("type"));
        assertEquals("boolean", props.get("active").get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_listParam_producesArrayWithItems() throws Exception {
        Method m = SampleTools.class.getMethod("withList", List.class);
        Map<String, Object> schema = generator.generate(m);

        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");
        Map<String, Object> tagsProp = props.get("tags");

        assertNotNull(tagsProp);
        assertEquals("array", tagsProp.get("type"));
        Map<String, Object> items = (Map<String, Object>) tagsProp.get("items");
        assertEquals("string", items.get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_arrayParam_producesArrayWithItems() throws Exception {
        Method m = SampleTools.class.getMethod("withArray", String[].class);
        Map<String, Object> schema = generator.generate(m);

        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");
        Map<String, Object> itemsProp = props.get("items");

        assertNotNull(itemsProp);
        assertEquals("array", itemsProp.get("type"));
        Map<String, Object> items = (Map<String, Object>) itemsProp.get("items");
        assertEquals("string", items.get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_pojoParam_producesObjectWithProperties() throws Exception {
        Method m = SampleTools.class.getMethod("withPojo", Address.class);
        Map<String, Object> schema = generator.generate(m);

        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");
        Map<String, Object> addressProp = props.get("address");

        assertNotNull(addressProp);
        assertEquals("object", addressProp.get("type"));
        Map<String, Object> addressProps = (Map<String, Object>) addressProp.get("properties");
        assertNotNull(addressProps);
        Map<String, Object> streetSchema = (Map<String, Object>) addressProps.get("street");
        assertEquals("string", streetSchema.get("type"));
        Map<String, Object> numberSchema = (Map<String, Object>) addressProps.get("number");
        assertEquals("integer", numberSchema.get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_mcpParam_respectsNameDescriptionRequired() throws Exception {
        Method m = SampleTools.class.getMethod("withMcpParam", String.class, int.class);
        Map<String, Object> schema = generator.generate(m);

        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");

        // Custom name used
        assertTrue(props.containsKey("user_name"));
        assertEquals("The user name", props.get("user_name").get("description"));

        // age has description
        assertTrue(props.containsKey("age"));
        assertEquals("Optional age", props.get("age").get("description"));

        // Required: only user_name (required=true), not age (required=false)
        List<String> required = (List<String>) schema.get("required");
        assertNotNull(required);
        assertTrue(required.contains("user_name"));
        assertFalse(required.contains("age"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_noAnnotation_paramIsRequired() throws Exception {
        Method m = SampleTools.class.getMethod("noAnnotation", String.class);
        Map<String, Object> schema = generator.generate(m);

        // Without -parameters flag, Java param name will be arg0
        List<String> required = (List<String>) schema.get("required");
        assertNotNull(required);
        assertEquals(1, required.size());
        // The parameter name is whatever Java reflection provides (arg0 without -parameters)
        String paramName = required.get(0);
        assertNotNull(paramName);
        assertFalse(paramName.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_customNameViaMcpParam() throws Exception {
        Method m = SampleTools.class.getMethod("customName", String.class);
        Map<String, Object> schema = generator.generate(m);

        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");
        assertTrue(props.containsKey("custom"));
        assertEquals(1, props.size()); // only "custom", not the Java param name
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_mixedParams_correctRequiredArray() throws Exception {
        Method m = SampleTools.class.getMethod("mixedParams",
                String.class, String.class, String.class);
        Map<String, Object> schema = generator.generate(m);

        List<String> required = (List<String>) schema.get("required");
        assertNotNull(required);
        assertEquals(2, required.size());
        assertTrue(required.contains("required1"));
        assertTrue(required.contains("required2"));
        assertFalse(required.contains("optional1"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void generate_onlyOptionalParams_noRequiredArray() throws Exception {
        Method m = SampleTools.class.getMethod("onlyOptional", String.class, String.class);
        Map<String, Object> schema = generator.generate(m);

        // When all params are optional, required array should not be present
        assertNull(schema.get("required"));
        Map<String, Map<String, Object>> props = (Map<String, Map<String, Object>>) schema.get("properties");
        assertEquals(2, props.size());
    }

    @Test
    void generate_alwaysReturnsTypeObject() throws Exception {
        Method m = SampleTools.class.getMethod("primitiveTypes",
                String.class, int.class, long.class, double.class, float.class, boolean.class);
        Map<String, Object> schema = generator.generate(m);
        assertEquals("object", schema.get("type"));
    }

    @Test
    void mapType_string() {
        Map<String, Object> result = generator.mapType(String.class, String.class);
        assertEquals("string", result.get("type"));
    }

    @Test
    void mapType_intPrimitive() {
        Map<String, Object> result = generator.mapType(int.class, int.class);
        assertEquals("integer", result.get("type"));
    }

    @Test
    void mapType_booleanWrapper() {
        Map<String, Object> result = generator.mapType(Boolean.class, Boolean.class);
        assertEquals("boolean", result.get("type"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mapPojo_generatesObjectWithFieldProperties() {
        Map<String, Object> result = generator.mapPojo(Address.class);
        assertEquals("object", result.get("type"));
        Map<String, Object> props = (Map<String, Object>) result.get("properties");
        assertNotNull(props);
        assertEquals(2, props.size());
    }

    @Test
    void mapType_doubleWrapper() {
        Map<String, Object> result = generator.mapType(Double.class, Double.class);
        assertEquals("number", result.get("type"));
    }

    @Test
    void mapType_floatPrimitive() {
        Map<String, Object> result = generator.mapType(float.class, float.class);
        assertEquals("number", result.get("type"));
    }

    @Test
    void mapType_longWrapper() {
        Map<String, Object> result = generator.mapType(Long.class, Long.class);
        assertEquals("integer", result.get("type"));
    }
}
