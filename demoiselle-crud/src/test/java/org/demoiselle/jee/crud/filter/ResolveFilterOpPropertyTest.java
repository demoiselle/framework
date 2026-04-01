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
 * Property-based test for {@link AbstractDAO#resolveFilterOp(String, String, TreeNodeField)}.
 *
 * <p><b>Validates: Requirement 7.1</b></p>
 */
class ResolveFilterOpPropertyTest {

    /**
     * Minimal test subclass that exposes the protected resolveFilterOp method.
     * EntityManager returns null since the paths under test (null parent,
     * non-enum/non-UUID values) do not require entity metadata.
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
     * Property 7: For any pair (key, value) where key is non-null and non-empty,
     * resolveFilterOp() returns a non-null instance of FilterOp.
     *
     * <p>We generate arbitrary non-empty keys and a wide variety of value strings
     * including null, "null", wildcards, boolean literals, and plain strings.
     * The parent is null (first-level filter). We avoid values that would trigger
     * enum/UUID detection since those paths require entity metadata from
     * EntityManager.</p>
     */
    @Property(tries = 500)
    void resolveFilterOpAlwaysReturnsNonNullFilterOp(
            @ForAll("nonEmptyKeys") String key,
            @ForAll("filterValues") String value) {

        FilterOp result = dao.callResolveFilterOp(key, value, null);

        assertNotNull(result,
                "resolveFilterOp() must never return null for key='" + key + "', value='" + value + "'");
        assertNotNull(result.key(),
                "FilterOp.key() must never be null");
        assertEquals(key, result.key(),
                "FilterOp.key() must match the input key");
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
     * Generates a wide variety of filter values that exercise all branches
     * of resolveFilterOp() that don't require entity metadata:
     * <ul>
     *   <li>null</li>
     *   <li>"null" literal</li>
     *   <li>wildcard patterns (*prefix, suffix*, *both*)</li>
     *   <li>boolean literals: "true", "false", "isTrue", "isFalse" (various cases)</li>
     *   <li>plain strings (fallback to Equals)</li>
     *   <li>empty string</li>
     *   <li>numeric strings</li>
     * </ul>
     */
    @Provide
    Arbitrary<String> filterValues() {
        return Arbitraries.oneOf(
                // null value (resolves to IsNull)
                Arbitraries.just(null),
                // "null" string literal (resolves to IsNull)
                Arbitraries.just("null"),
                // wildcard prefix (resolves to Like)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
                        .map(s -> "*" + s),
                // wildcard suffix (resolves to Like)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
                        .map(s -> s + "*"),
                // wildcard both (resolves to Like)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(20)
                        .map(s -> "*" + s + "*"),
                // boolean true variants (resolves to IsTrue)
                Arbitraries.of("true", "True", "TRUE", "isTrue", "IsTrue", "ISTRUE"),
                // boolean false variants (resolves to IsFalse)
                Arbitraries.of("false", "False", "FALSE", "isFalse", "IsFalse", "ISFALSE"),
                // plain strings (resolves to Equals)
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30),
                // empty string (resolves to Equals)
                Arbitraries.just(""),
                // numeric strings (resolves to Equals)
                Arbitraries.integers().between(0, 99999).map(String::valueOf)
        );
    }
}
