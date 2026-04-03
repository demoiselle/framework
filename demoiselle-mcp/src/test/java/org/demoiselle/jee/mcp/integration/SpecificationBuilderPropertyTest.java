/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.mcp.integration;

import net.jqwik.api.*;

import org.demoiselle.jee.mcp.integration.SpecificationBuilder.FilterDescriptor;
import org.demoiselle.jee.mcp.integration.SpecificationBuilder.Operator;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link SpecificationBuilder}.
 *
 * <p>Covers:</p>
 * <ul>
 *   <li>Property 24: SpecificationBuilder suporta todos os operadores</li>
 * </ul>
 */
class SpecificationBuilderPropertyTest {

    // ── Test entity ──

    @SuppressWarnings("unused")
    static class SampleEntity {
        String name;
        int age;
        double score;
        String email;
        boolean active;
    }

    // ── Arbitraries ──

    @Provide
    Arbitrary<String> validFields() {
        return Arbitraries.of("name", "age", "score", "email", "active");
    }

    @Provide
    Arbitrary<String> invalidFields() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(15)
                .filter(s -> !List.of("name", "age", "score", "email", "active").contains(s));
    }

    @Provide
    Arbitrary<String> comparisonOperators() {
        return Arbitraries.of("eq", "ne", "gt", "ge", "lt", "le");
    }

    @Provide
    Arbitrary<String> allOperatorStrings() {
        return Arbitraries.of("eq", "ne", "gt", "ge", "lt", "le", "like", "in", "isNull");
    }

    // -----------------------------------------------------------------------
    // Property 24: SpecificationBuilder suporta todos os operadores
    // -----------------------------------------------------------------------

    /**
     * For every supported comparison operator and a valid field, the builder
     * must produce a FilterDescriptor with the correct operator and field.
     *
     * <p><b>Validates: Requirements 17.2, 17.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 24: SpecificationBuilder suporta todos os operadores
    void comparisonOperatorsProduceValidFilters(
            @ForAll("validFields") String field,
            @ForAll("comparisonOperators") String operatorStr,
            @ForAll @From("stringValues") String value) {

        SpecificationBuilder<SampleEntity> builder = new SpecificationBuilder<>();

        List<Map<String, Object>> filters = List.of(
                Map.of("field", field, "operator", operatorStr, "value", value)
        );

        List<FilterDescriptor> descriptors = builder.build(SampleEntity.class, filters);

        assertEquals(1, descriptors.size(), "Must produce exactly one filter");
        FilterDescriptor fd = descriptors.get(0);
        assertEquals(field, fd.field(), "Field must match");
        assertEquals(Operator.fromString(operatorStr), fd.operator(), "Operator must match");
        assertEquals(value, fd.value(), "Value must match");
    }

    /**
     * The LIKE operator must produce a valid filter with a string value.
     *
     * <p><b>Validates: Requirements 17.2, 17.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 24: SpecificationBuilder suporta todos os operadores
    void likeOperatorProducesValidFilter(
            @ForAll("validFields") String field,
            @ForAll @From("stringValues") String value) {

        SpecificationBuilder<SampleEntity> builder = new SpecificationBuilder<>();

        List<Map<String, Object>> filters = List.of(
                Map.of("field", field, "operator", "like", "value", "%" + value + "%")
        );

        List<FilterDescriptor> descriptors = builder.build(SampleEntity.class, filters);

        assertEquals(1, descriptors.size());
        assertEquals(Operator.LIKE, descriptors.get(0).operator());
    }

    /**
     * The IN operator must produce a valid filter when given a collection value.
     *
     * <p><b>Validates: Requirements 17.2, 17.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 24: SpecificationBuilder suporta todos os operadores
    void inOperatorProducesValidFilter(
            @ForAll("validFields") String field) {

        SpecificationBuilder<SampleEntity> builder = new SpecificationBuilder<>();

        List<String> values = List.of("a", "b", "c");
        List<Map<String, Object>> filters = List.of(
                Map.of("field", field, "operator", "in", "value", values)
        );

        List<FilterDescriptor> descriptors = builder.build(SampleEntity.class, filters);

        assertEquals(1, descriptors.size());
        assertEquals(Operator.IN, descriptors.get(0).operator());
        assertEquals(values, descriptors.get(0).value());
    }

    /**
     * The isNull operator must produce a valid filter without requiring a value.
     *
     * <p><b>Validates: Requirements 17.2, 17.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 24: SpecificationBuilder suporta todos os operadores
    void isNullOperatorProducesValidFilterWithoutValue(
            @ForAll("validFields") String field) {

        SpecificationBuilder<SampleEntity> builder = new SpecificationBuilder<>();

        // isNull does not require a value — use a mutable map to allow null
        Map<String, Object> filter = new java.util.HashMap<>();
        filter.put("field", field);
        filter.put("operator", "isNull");
        filter.put("value", null);

        List<FilterDescriptor> descriptors = builder.build(
                SampleEntity.class, List.of(filter));

        assertEquals(1, descriptors.size());
        assertEquals(Operator.IS_NULL, descriptors.get(0).operator());
        assertNull(descriptors.get(0).value());
    }

    /**
     * For any field name that does not exist on the entity, the builder must
     * reject the filter with an IllegalArgumentException.
     *
     * <p><b>Validates: Requirements 17.3</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 24: SpecificationBuilder suporta todos os operadores
    void invalidFieldIsRejected(
            @ForAll("invalidFields") String invalidField,
            @ForAll("allOperatorStrings") String operatorStr) {

        SpecificationBuilder<SampleEntity> builder = new SpecificationBuilder<>();

        Map<String, Object> filter = new java.util.HashMap<>();
        filter.put("field", invalidField);
        filter.put("operator", operatorStr);
        filter.put("value", "test");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> builder.build(SampleEntity.class, List.of(filter)));

        assertTrue(ex.getMessage().contains(invalidField),
                "Error message must mention the invalid field name");
    }

    /**
     * All operator strings must be parseable by Operator.fromString.
     *
     * <p><b>Validates: Requirements 17.4</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 24: SpecificationBuilder suporta todos os operadores
    void allOperatorStringsAreParseable(
            @ForAll("allOperatorStrings") String operatorStr) {

        Operator op = Operator.fromString(operatorStr);
        assertNotNull(op, "Operator must be parseable: " + operatorStr);
    }

    /**
     * The matches() method must correctly apply EQ filters against records.
     *
     * <p><b>Validates: Requirements 17.2</b></p>
     */
    @Property(tries = 100)
    // Feature: demoiselle-mcp, Property 24: SpecificationBuilder suporta todos os operadores
    void matchesAppliesEqFilterCorrectly(
            @ForAll @From("stringValues") String value) {

        SpecificationBuilder<SampleEntity> builder = new SpecificationBuilder<>();

        FilterDescriptor filter = new FilterDescriptor("name", Operator.EQ, value);

        Map<String, Object> matchingRecord = Map.of("name", value);
        Map<String, Object> nonMatchingRecord = Map.of("name", value + "_different");

        assertTrue(builder.matches(matchingRecord, List.of(filter)),
                "Record with matching value must match EQ filter");
        assertFalse(builder.matches(nonMatchingRecord, List.of(filter)),
                "Record with different value must not match EQ filter");
    }

    @Provide
    Arbitrary<String> stringValues() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }
}
