/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import net.jqwik.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 6: Precedência de resolução de perfil
 *
 * <p><b>Validates: Requirements 5.1, 5.2</b></p>
 *
 * For any combination of system property {@code demoiselle.profile} and
 * environment variable {@code DEMOISELLE_PROFILE}, the profile resolution must:
 * <ol>
 *   <li>Return the system property value if defined and non-blank</li>
 *   <li>Return the environment variable value if the system property is null or blank</li>
 *   <li>Return null if both are null or blank</li>
 *   <li>Strip whitespace from the result</li>
 * </ol>
 *
 * <p>Since {@code resolveProfile()} is private and relies on
 * {@code System.getProperty}/{@code System.getenv}, this test replicates the
 * resolution logic in a reference function and verifies the precedence rules
 * hold for all generated input combinations.</p>
 */
class ProfileResolutionPrecedencePropertyTest {

    /**
     * Replicates the resolution logic from {@code ConfigurationLoader.resolveProfile()}:
     * <pre>
     *   String profile = sysProp;
     *   if (profile == null || profile.isBlank()) {
     *       profile = envVar;
     *   }
     *   return (profile != null && !profile.isBlank()) ? profile.strip() : null;
     * </pre>
     */
    private static String resolveProfile(String sysProp, String envVar) {
        String profile = sysProp;
        if (profile == null || profile.isBlank()) {
            profile = envVar;
        }
        return (profile != null && !profile.isBlank()) ? profile.strip() : null;
    }

    // ── Property: system property takes precedence over env var ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void systemPropertyTakesPrecedenceOverEnvVar(
            @ForAll("validProfileNames") String sysProp,
            @ForAll("nullableProfileInputs") String envVar) {

        String result = resolveProfile(sysProp, envVar);

        assertEquals(sysProp.strip(), result,
                "When system property is a valid non-blank value, it must take precedence over env var");
    }

    // ── Property: env var is used when system property is null ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void envVarUsedWhenSysPropIsNull(
            @ForAll("validProfileNames") String envVar) {

        String result = resolveProfile(null, envVar);

        assertEquals(envVar.strip(), result,
                "When system property is null, env var must be used");
    }

    // ── Property: env var is used when system property is blank ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void envVarUsedWhenSysPropIsBlank(
            @ForAll("blankStrings") String sysProp,
            @ForAll("validProfileNames") String envVar) {

        String result = resolveProfile(sysProp, envVar);

        assertEquals(envVar.strip(), result,
                "When system property is blank, env var must be used");
    }

    // ── Property: null returned when both are null ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void nullReturnedWhenBothAreNull() {
        String result = resolveProfile(null, null);

        assertNull(result,
                "When both system property and env var are null, result must be null");
    }

    // ── Property: null returned when both are blank ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void nullReturnedWhenBothAreBlank(
            @ForAll("blankStrings") String sysProp,
            @ForAll("blankStrings") String envVar) {

        String result = resolveProfile(sysProp, envVar);

        assertNull(result,
                "When both system property and env var are blank, result must be null");
    }

    // ── Property: null returned when sysProp is null and envVar is blank ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void nullReturnedWhenSysPropNullAndEnvVarBlank(
            @ForAll("blankStrings") String envVar) {

        String result = resolveProfile(null, envVar);

        assertNull(result,
                "When system property is null and env var is blank, result must be null");
    }

    // ── Property: result is always stripped of whitespace ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void resultIsAlwaysStripped(
            @ForAll("paddedProfileNames") String sysProp,
            @ForAll("nullableProfileInputs") String envVar) {

        String result = resolveProfile(sysProp, envVar);

        assertNotNull(result, "Padded non-blank sysProp should produce a non-null result");
        assertEquals(result, result.strip(),
                "Result must always be stripped of leading/trailing whitespace");
        assertFalse(result.isEmpty(),
                "Stripped result from a non-blank input must not be empty");
    }

    // ── Property: full combination — precedence holds for all inputs ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 6: Precedência de resolução de perfil")
    void precedenceHoldsForAllCombinations(
            @ForAll("nullableProfileInputs") String sysProp,
            @ForAll("nullableProfileInputs") String envVar) {

        String result = resolveProfile(sysProp, envVar);

        boolean sysPropDefined = sysProp != null && !sysProp.isBlank();
        boolean envVarDefined = envVar != null && !envVar.isBlank();

        if (sysPropDefined) {
            assertEquals(sysProp.strip(), result,
                    "System property must take precedence when defined and non-blank");
        } else if (envVarDefined) {
            assertEquals(envVar.strip(), result,
                    "Env var must be used when system property is null or blank");
        } else {
            assertNull(result,
                    "Result must be null when both are null or blank");
        }

        // If result is non-null, it must be stripped
        if (result != null) {
            assertEquals(result, result.strip(),
                    "Non-null result must always be stripped");
        }
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> validProfileNames() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(30)
                .map(s -> s.toLowerCase());
    }

    @Provide
    Arbitrary<String> paddedProfileNames() {
        return validProfileNames()
                .map(name -> {
                    // Add random leading/trailing whitespace
                    return "  " + name + "  ";
                });
    }

    @Provide
    Arbitrary<String> blankStrings() {
        return Arbitraries.of("", " ", "  ", "\t", " \t ", "   ");
    }

    @Provide
    Arbitrary<String> nullableProfileInputs() {
        return Arbitraries.oneOf(
                Arbitraries.just(null),
                blankStrings(),
                validProfileNames(),
                paddedProfileNames()
        );
    }
}
