/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.rest.filter;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import net.jqwik.api.*;

import org.demoiselle.jee.rest.annotation.CacheControl;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based tests for {@link CacheControlBuilder}.
 *
 * Uses a proxy-based stub to create arbitrary {@link CacheControl} annotation
 * instances for property testing.
 */
class CacheControlBuilderPropertyTest {

    // ---------------------------------------------------------------
    // Stub helper — creates a CacheControl annotation via defaults map
    // ---------------------------------------------------------------

    private static CacheControl stub(String value, int maxAge, int sMaxAge,
                                     boolean noCache, boolean noStore,
                                     boolean mustRevalidate, boolean isPrivate) {
        return new CacheControl() {
            @Override public Class<? extends Annotation> annotationType() { return CacheControl.class; }
            @Override public String value()          { return value; }
            @Override public int maxAge()             { return maxAge; }
            @Override public int sMaxAge()            { return sMaxAge; }
            @Override public boolean noCache()        { return noCache; }
            @Override public boolean noStore()        { return noStore; }
            @Override public boolean mustRevalidate() { return mustRevalidate; }
            @Override public boolean isPrivate()      { return isPrivate; }
        };
    }

    // ---------------------------------------------------------------
    // Providers
    // ---------------------------------------------------------------

    @Provide
    Arbitrary<String> nonEmptyValues() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(80)
                .filter(s -> !s.isEmpty());
    }

    @Provide
    Arbitrary<int[]> typedAttributes() {
        // [maxAge, sMaxAge, noCache(0/1), noStore(0/1), mustRevalidate(0/1), isPrivate(0/1)]
        Arbitrary<Integer> ages = Arbitraries.oneOf(
                Arbitraries.just(-1),
                Arbitraries.integers().between(0, 86400)
        );
        Arbitrary<Boolean> bools = Arbitraries.of(true, false);

        return Combinators.combine(ages, ages, bools, bools, bools, bools)
                .as((ma, sma, nc, ns, mr, ip) ->
                        new int[]{ ma, sma, nc ? 1 : 0, ns ? 1 : 0, mr ? 1 : 0, ip ? 1 : 0 });
    }

    // ---------------------------------------------------------------
    // Property 8: value() precedence
    // ---------------------------------------------------------------

    /**
     * // Feature: rest-enhancements, Property 8: Precedência do value() no CacheControlBuilder
     *
     * For any @CacheControl with non-empty value(), build() returns exactly
     * value(), regardless of typed attributes.
     *
     * **Validates: Requirements 6.1**
     */
    @Property(tries = 100)
    void valueTakesPrecedenceOverTypedAttributes(
            @ForAll("nonEmptyValues") String value,
            @ForAll("typedAttributes") int[] attrs) {

        CacheControl cc = stub(value, attrs[0], attrs[1],
                attrs[2] == 1, attrs[3] == 1, attrs[4] == 1, attrs[5] == 1);

        assertEquals(value, CacheControlBuilder.build(cc),
                "build() must return value() verbatim when it is non-empty");
    }

    // ---------------------------------------------------------------
    // Property 9: Typed directives correct
    // ---------------------------------------------------------------

    /**
     * // Feature: rest-enhancements, Property 9: Atributos tipados produzem diretivas corretas
     *
     * For any combination of typed attributes (with value() empty):
     * the resulting string contains exactly the expected directives.
     *
     * **Validates: Requirements 6.2, 6.3, 6.4, 6.5, 6.6, 6.7, 6.8**
     */
    @Property(tries = 100)
    void typedAttributesProduceCorrectDirectives(@ForAll("typedAttributes") int[] attrs) {
        int maxAge = attrs[0];
        int sMaxAge = attrs[1];
        boolean noCache = attrs[2] == 1;
        boolean noStore = attrs[3] == 1;
        boolean mustRevalidate = attrs[4] == 1;
        boolean isPrivate = attrs[5] == 1;

        CacheControl cc = stub("", maxAge, sMaxAge, noCache, noStore, mustRevalidate, isPrivate);
        String result = CacheControlBuilder.build(cc);

        // Check each directive presence/absence
        if (maxAge >= 0) {
            assertTrue(result.contains("max-age=" + maxAge),
                    "Expected max-age=" + maxAge + " in: " + result);
        }
        if (sMaxAge >= 0) {
            assertTrue(result.contains("s-maxage=" + sMaxAge),
                    "Expected s-maxage=" + sMaxAge + " in: " + result);
        }
        assertEquals(noCache, result.contains("no-cache"),
                "no-cache presence mismatch in: " + result);
        assertEquals(noStore, result.contains("no-store"),
                "no-store presence mismatch in: " + result);
        assertEquals(mustRevalidate, result.contains("must-revalidate"),
                "must-revalidate presence mismatch in: " + result);

        // private/public logic
        if (isPrivate) {
            assertTrue(result.contains("private"),
                    "Expected 'private' in: " + result);
            assertFalse(result.contains("public"),
                    "Should not contain 'public' when isPrivate=true in: " + result);
        } else {
            // "public" only when at least one other directive is present
            boolean hasOtherDirective = maxAge >= 0 || sMaxAge >= 0
                    || noCache || noStore || mustRevalidate;
            assertEquals(hasOtherDirective, result.contains("public"),
                    "public presence mismatch in: " + result);
        }
    }

    // ---------------------------------------------------------------
    // Property 10: Round-trip build/parse
    // ---------------------------------------------------------------

    /**
     * // Feature: rest-enhancements, Property 10: Round-trip de construção/parsing do Cache-Control
     *
     * For any combination of typed attributes, build the string via
     * CacheControlBuilder and parse it back — the parsed directives must
     * be equivalent to the originals.
     *
     * **Validates: Requirements 6.10**
     */
    @Property(tries = 100)
    void roundTripBuildAndParse(@ForAll("typedAttributes") int[] attrs) {
        int maxAge = attrs[0];
        int sMaxAge = attrs[1];
        boolean noCache = attrs[2] == 1;
        boolean noStore = attrs[3] == 1;
        boolean mustRevalidate = attrs[4] == 1;
        boolean isPrivate = attrs[5] == 1;

        CacheControl cc = stub("", maxAge, sMaxAge, noCache, noStore, mustRevalidate, isPrivate);
        String header = CacheControlBuilder.buildFromTypedAttributes(cc);

        // Parse the header back into a map of directives
        Map<String, String> parsed = parseDirectives(header);

        // Verify round-trip equivalence
        boolean hasAnyDirective = maxAge >= 0 || sMaxAge >= 0
                || noCache || noStore || mustRevalidate || isPrivate;

        if (!hasAnyDirective) {
            // Default fallback: "max-age=0"
            assertEquals("0", parsed.get("max-age"),
                    "Default should be max-age=0");
        } else {
            if (maxAge >= 0) {
                assertEquals(String.valueOf(maxAge), parsed.get("max-age"),
                        "max-age round-trip failed");
            }
            if (sMaxAge >= 0) {
                assertEquals(String.valueOf(sMaxAge), parsed.get("s-maxage"),
                        "s-maxage round-trip failed");
            }
            assertEquals(noCache, parsed.containsKey("no-cache"),
                    "no-cache round-trip failed");
            assertEquals(noStore, parsed.containsKey("no-store"),
                    "no-store round-trip failed");
            assertEquals(mustRevalidate, parsed.containsKey("must-revalidate"),
                    "must-revalidate round-trip failed");
            assertEquals(isPrivate, parsed.containsKey("private"),
                    "private round-trip failed");
            if (!isPrivate) {
                assertTrue(parsed.containsKey("public"),
                        "public should be present when isPrivate=false and directives exist");
            }
        }
    }

    /**
     * Parses a Cache-Control header string into a map of directive-name to
     * directive-value (or empty string for valueless directives like "no-cache").
     */
    private static Map<String, String> parseDirectives(String header) {
        Map<String, String> map = new LinkedHashMap<>();
        String[] parts = header.split(",");
        for (String part : parts) {
            String trimmed = part.trim();
            int eq = trimmed.indexOf('=');
            if (eq >= 0) {
                map.put(trimmed.substring(0, eq).trim(), trimmed.substring(eq + 1).trim());
            } else {
                map.put(trimmed, "");
            }
        }
        return map;
    }
}
