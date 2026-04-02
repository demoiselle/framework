/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import jakarta.persistence.EntityManager;

import net.jqwik.api.*;
import net.jqwik.api.constraints.*;

import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.TreeNodeField;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for FilterOp operator prefix resolution in
 * {@link AbstractDAO#resolveFilterOp(String, String, TreeNodeField)}.
 *
 * <p><b>Validates: Requirements 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.15</b></p>
 */
class FilterOpPrefixPropertyTest {

    static class TestableDAO extends AbstractDAO<Object, Long> {
        @Override
        public EntityManager getEntityManager() {
            return null;
        }

        public FilterOp callResolveFilterOp(String key, String value,
                TreeNodeField<String, Set<String>> parent) {
            return resolveFilterOp(key, value, parent);
        }
    }

    private final TestableDAO dao = new TestableDAO();

    // -----------------------------------------------------------------------
    // Property 17: resolveFilterOp resolve prefixos de operador corretamente
    // Feature: crud-enhancements, Property 17: resolveFilterOp resolve prefixos de operador corretamente
    // **Validates: Requirements 6.2, 6.3, 6.4, 6.5, 6.6, 6.7**
    // -----------------------------------------------------------------------

    @Property(tries = 200)
    void gtPrefixResolvesToGreaterThan(
            @ForAll("alphaKeys") String key,
            @ForAll("nonEmptyAlpha") String suffix) {

        String value = "gt:" + suffix;
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertInstanceOf(FilterOp.GreaterThan.class, result,
                "gt: prefix must resolve to GreaterThan for value='" + value + "'");
        FilterOp.GreaterThan gt = (FilterOp.GreaterThan) result;
        assertEquals(key, gt.key());
        assertEquals(suffix, gt.value());
    }

    @Property(tries = 200)
    void ltPrefixResolvesToLessThan(
            @ForAll("alphaKeys") String key,
            @ForAll("nonEmptyAlpha") String suffix) {

        String value = "lt:" + suffix;
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertInstanceOf(FilterOp.LessThan.class, result,
                "lt: prefix must resolve to LessThan for value='" + value + "'");
        FilterOp.LessThan lt = (FilterOp.LessThan) result;
        assertEquals(key, lt.key());
        assertEquals(suffix, lt.value());
    }

    @Property(tries = 200)
    void gtePrefixResolvesToGreaterThanOrEqual(
            @ForAll("alphaKeys") String key,
            @ForAll("nonEmptyAlpha") String suffix) {

        String value = "gte:" + suffix;
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertInstanceOf(FilterOp.GreaterThanOrEqual.class, result,
                "gte: prefix must resolve to GreaterThanOrEqual for value='" + value + "'");
        FilterOp.GreaterThanOrEqual gte = (FilterOp.GreaterThanOrEqual) result;
        assertEquals(key, gte.key());
        assertEquals(suffix, gte.value());
    }

    @Property(tries = 200)
    void ltePrefixResolvesToLessThanOrEqual(
            @ForAll("alphaKeys") String key,
            @ForAll("nonEmptyAlpha") String suffix) {

        String value = "lte:" + suffix;
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertInstanceOf(FilterOp.LessThanOrEqual.class, result,
                "lte: prefix must resolve to LessThanOrEqual for value='" + value + "'");
        FilterOp.LessThanOrEqual lte = (FilterOp.LessThanOrEqual) result;
        assertEquals(key, lte.key());
        assertEquals(suffix, lte.value());
    }

    @Property(tries = 200)
    void betweenPrefixResolvesToBetween(
            @ForAll("alphaKeys") String key,
            @ForAll("nonEmptyAlpha") String lower,
            @ForAll("nonEmptyAlpha") String upper) {

        String value = "between:" + lower + "," + upper;
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertInstanceOf(FilterOp.Between.class, result,
                "between: prefix must resolve to Between for value='" + value + "'");
        FilterOp.Between btw = (FilterOp.Between) result;
        assertEquals(key, btw.key());
        assertEquals(lower, btw.lower());
        assertEquals(upper, btw.upper());
    }

    @Property(tries = 200)
    void inPrefixResolvesToIn(
            @ForAll("alphaKeys") String key,
            @ForAll("inValueList") List<String> items) {

        String value = "in:" + String.join(",", items);
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertInstanceOf(FilterOp.In.class, result,
                "in: prefix must resolve to In for value='" + value + "'");
        FilterOp.In in = (FilterOp.In) result;
        assertEquals(key, in.key());
        assertEquals(items.size(), in.values().size());
        for (int i = 0; i < items.size(); i++) {
            assertEquals(items.get(i), in.values().get(i));
        }
    }

    // -----------------------------------------------------------------------
    // Property 18: Prefixos de operador têm precedência sobre filtros existentes
    // Feature: crud-enhancements, Property 18: Prefixos de operador têm precedência sobre filtros existentes
    // **Validates: Requirements 6.15**
    // -----------------------------------------------------------------------

    @Property(tries = 200)
    void operatorPrefixTakesPrecedenceOverLikePattern(
            @ForAll("alphaKeys") String key,
            @ForAll("operatorPrefixes") String prefix,
            @ForAll("likePatterns") String likeSuffix) {

        String value = prefix + likeSuffix;
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertFalse(result instanceof FilterOp.Like,
                "Operator prefix '" + prefix + "' must take precedence over Like pattern. "
                + "Got Like for value='" + value + "'");
        assertCorrectOperatorType(result, prefix);
    }

    @Property(tries = 200)
    void operatorPrefixTakesPrecedenceOverBooleanLiterals(
            @ForAll("alphaKeys") String key,
            @ForAll("operatorPrefixes") String prefix,
            @ForAll("booleanLiterals") String boolSuffix) {

        String value = prefix + boolSuffix;
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertFalse(result instanceof FilterOp.IsTrue,
                "Operator prefix '" + prefix + "' must take precedence over IsTrue. "
                + "Got IsTrue for value='" + value + "'");
        assertFalse(result instanceof FilterOp.IsFalse,
                "Operator prefix '" + prefix + "' must take precedence over IsFalse. "
                + "Got IsFalse for value='" + value + "'");
        assertCorrectOperatorType(result, prefix);
    }

    @Property(tries = 200)
    void operatorPrefixTakesPrecedenceOverNullLiteral(
            @ForAll("alphaKeys") String key,
            @ForAll("singleValuePrefixes") String prefix) {

        // Value after prefix is "null" — should NOT resolve to IsNull
        String value = prefix + "null";
        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertFalse(result instanceof FilterOp.IsNull,
                "Operator prefix '" + prefix + "' must take precedence over IsNull. "
                + "Got IsNull for value='" + value + "'");
        assertCorrectOperatorType(result, prefix);
    }

    // -----------------------------------------------------------------------
    // Providers
    // -----------------------------------------------------------------------

    @Provide
    Arbitrary<String> alphaKeys() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<String> nonEmptyAlpha() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20);
    }

    @Provide
    Arbitrary<List<String>> inValueList() {
        return Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10)
                .list().ofMinSize(1).ofMaxSize(5);
    }

    @Provide
    Arbitrary<String> operatorPrefixes() {
        return Arbitraries.of("gt:", "lt:", "gte:", "lte:");
    }

    @Provide
    Arbitrary<String> singleValuePrefixes() {
        return Arbitraries.of("gt:", "lt:", "gte:", "lte:");
    }

    @Provide
    Arbitrary<String> likePatterns() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10).map(s -> "*" + s),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10).map(s -> s + "*"),
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(10).map(s -> "*" + s + "*")
        );
    }

    @Provide
    Arbitrary<String> booleanLiterals() {
        return Arbitraries.of("true", "True", "TRUE", "false", "False", "FALSE",
                "isTrue", "IsTrue", "isFalse", "IsFalse");
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private void assertCorrectOperatorType(FilterOp result, String prefix) {
        switch (prefix) {
            case "gt:" -> assertInstanceOf(FilterOp.GreaterThan.class, result,
                    "Expected GreaterThan for prefix 'gt:'");
            case "lt:" -> assertInstanceOf(FilterOp.LessThan.class, result,
                    "Expected LessThan for prefix 'lt:'");
            case "gte:" -> assertInstanceOf(FilterOp.GreaterThanOrEqual.class, result,
                    "Expected GreaterThanOrEqual for prefix 'gte:'");
            case "lte:" -> assertInstanceOf(FilterOp.LessThanOrEqual.class, result,
                    "Expected LessThanOrEqual for prefix 'lte:'");
            case "between:" -> assertInstanceOf(FilterOp.Between.class, result,
                    "Expected Between for prefix 'between:'");
            case "in:" -> assertInstanceOf(FilterOp.In.class, result,
                    "Expected In for prefix 'in:'");
            default -> fail("Unknown prefix: " + prefix);
        }
    }
}
