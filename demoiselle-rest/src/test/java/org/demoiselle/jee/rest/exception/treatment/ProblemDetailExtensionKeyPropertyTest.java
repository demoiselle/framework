/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.exception.treatment;

// Feature: rfc-standards-compliance, Property 2: Validação de chaves de extensão do ProblemDetail

import net.jqwik.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based test for ProblemDetail extension key validation.
 *
 * <p><b>Validates: Requirements 1.3, 1.4</b></p>
 *
 * <ul>
 *   <li>1.3 — Extension fields with non-reserved keys are accepted and retrievable</li>
 *   <li>1.4 — Extension fields with reserved keys are rejected with IllegalArgumentException</li>
 * </ul>
 */
class ProblemDetailExtensionKeyPropertyTest {

    private static final Set<String> RESERVED_KEYS =
            Set.of("type", "title", "status", "detail", "instance");

    // ── Providers ──────────────────────────────────────────────────

    @Provide
    Arbitrary<String> reservedKeys() {
        return Arbitraries.of("type", "title", "status", "detail", "instance");
    }

    @Provide
    Arbitrary<String> nonReservedKeys() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .filter(k -> !RESERVED_KEYS.contains(k));
    }

    @Provide
    Arbitrary<Object> extensionValues() {
        return Arbitraries.oneOf(
                Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(30).map(s -> (Object) s),
                Arbitraries.integers().between(-1000, 1000).map(i -> (Object) i),
                Arbitraries.of(true, false).map(b -> (Object) b)
        );
    }

    // ── Property 2: Extension key validation ───────────────────────

    /**
     * For any string key that belongs to the reserved set
     * ("type", "title", "status", "detail", "instance"),
     * calling setExtension(key, value) must throw IllegalArgumentException.
     *
     * <p><b>Validates: Requirements 1.3, 1.4</b></p>
     */
    @Property(tries = 100)
    void reservedKeysMustBeRejected(
            @ForAll("reservedKeys") String key,
            @ForAll("extensionValues") Object value
    ) {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(200);

        assertThrows(IllegalArgumentException.class,
                () -> pd.setExtension(key, value),
                "setExtension must reject reserved key: " + key);
    }

    /**
     * For any string key that does NOT belong to the reserved set,
     * calling setExtension(key, value) must succeed and the value
     * must be retrievable via getExtensions().get(key).
     *
     * <p><b>Validates: Requirements 1.3, 1.4</b></p>
     */
    @Property(tries = 100)
    void nonReservedKeysMustBeAcceptedAndRetrievable(
            @ForAll("nonReservedKeys") String key,
            @ForAll("extensionValues") Object value
    ) {
        ProblemDetail pd = new ProblemDetail();
        pd.setStatus(200);

        // Must not throw
        pd.setExtension(key, value);

        assertTrue(pd.getExtensions().containsKey(key),
                "Extension key '" + key + "' must be present after setExtension");
        assertEquals(value, pd.getExtensions().get(key),
                "Extension value for key '" + key + "' must be retrievable");
    }
}
