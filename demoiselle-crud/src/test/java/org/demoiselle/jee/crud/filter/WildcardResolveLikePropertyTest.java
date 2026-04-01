/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.crud.filter;

import jakarta.persistence.EntityManager;

import net.jqwik.api.*;

import org.demoiselle.jee.crud.AbstractDAO;
import org.demoiselle.jee.crud.TreeNodeField;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test verifying that any value containing a wildcard (*)
 * causes {@link AbstractDAO#resolveFilterOp(String, String, TreeNodeField)}
 * to return {@link FilterOp.Like}.
 *
 * <p><b>Validates: Requirement 7.3</b></p>
 */
class WildcardResolveLikePropertyTest {

    /**
     * Minimal test subclass that exposes the protected resolveFilterOp method.
     * EntityManager returns null since wildcard detection does not require
     * entity metadata.
     */
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

    /**
     * Property 8: For any string value containing a wildcard (*),
     * resolveFilterOp() returns FilterOp.Like.
     *
     * <p>We generate arbitrary non-empty keys and values that always contain
     * at least one wildcard character (*) at the start, end, or both.
     * The parent is null (first-level filter). Values like "null" are excluded
     * since the null-check takes precedence over wildcard detection.</p>
     */
    @Property(tries = 500)
    void wildcardValueAlwaysResolvesToLike(
            @ForAll("nonEmptyKeys") String key,
            @ForAll("wildcardValues") String value) {

        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertInstanceOf(FilterOp.Like.class, result,
                "resolveFilterOp() must return FilterOp.Like for wildcard value='" + value + "'");

        FilterOp.Like like = (FilterOp.Like) result;
        assertEquals(key, like.key(),
                "FilterOp.Like.key() must match the input key");
        assertEquals(value, like.pattern(),
                "FilterOp.Like.pattern() must match the input value");
    }

    /**
     * Generates non-null, non-empty key strings (alphabetic identifiers).
     */
    @Provide
    Arbitrary<String> nonEmptyKeys() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30);
    }

    /**
     * Generates string values that always contain at least one wildcard (*).
     * Covers three patterns:
     * <ul>
     *   <li>Prefix wildcard: *suffix</li>
     *   <li>Suffix wildcard: prefix*</li>
     *   <li>Both wildcards: *middle*</li>
     * </ul>
     *
     * <p>The base strings are alphabetic to avoid accidentally generating
     * "null" which would be caught by the null-check before wildcard detection.</p>
     */
    @Provide
    Arbitrary<String> wildcardValues() {
        Arbitrary<String> base = Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20);

        return Arbitraries.oneOf(
                // *suffix
                base.map(s -> "*" + s),
                // prefix*
                base.map(s -> s + "*"),
                // *middle*
                base.map(s -> "*" + s + "*")
        );
    }
}
