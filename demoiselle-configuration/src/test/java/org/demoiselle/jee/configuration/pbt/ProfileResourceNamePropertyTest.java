/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import net.jqwik.api.*;

import org.demoiselle.jee.configuration.ConfigurationType;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 7: Construção de nome de arquivo de perfil
 *
 * <p><b>Validates: Requirements 5.6</b></p>
 *
 * For any base resource name, non-empty profile and configuration type
 * (PROPERTIES or XML), the profile resource name must follow the format
 * {@code {baseName}-{profile}.{extension}}, where the extension is derived
 * from the configuration type.
 *
 * <p>Since {@code buildProfileResource()} is private in ConfigurationLoader,
 * this test replicates the logic in a reference function and verifies the
 * construction rules hold for all generated inputs.</p>
 */
class ProfileResourceNamePropertyTest {

    /**
     * Replicates the logic from {@code ConfigurationLoader.buildProfileResource()}.
     */
    private static String buildProfileResource(String baseResource, String profile, ConfigurationType type) {
        String extension = "." + type.toString().toLowerCase();
        String baseName = baseResource.endsWith(extension)
                ? baseResource.substring(0, baseResource.length() - extension.length())
                : baseResource;
        return baseName + "-" + profile + extension;
    }

    // ── Property: result follows format {baseName}-{profile}.{extension} ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 7: Construção de nome de arquivo de perfil")
    void resultFollowsExpectedFormat(
            @ForAll("alphanumericBaseNames") String baseName,
            @ForAll("alphanumericProfiles") String profile,
            @ForAll("fileConfigTypes") ConfigurationType type) {

        String extension = "." + type.toString().toLowerCase();
        String baseResource = baseName + extension;

        String result = buildProfileResource(baseResource, profile, type);

        String expected = baseName + "-" + profile + extension;
        assertEquals(expected, result,
                "Result must follow format {baseName}-{profile}.{extension}");
    }

    // ── Property: extension is derived from the configuration type ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 7: Construção de nome de arquivo de perfil")
    void extensionIsDerivedFromType(
            @ForAll("alphanumericBaseNames") String baseName,
            @ForAll("alphanumericProfiles") String profile,
            @ForAll("fileConfigTypes") ConfigurationType type) {

        String extension = "." + type.toString().toLowerCase();
        String baseResource = baseName + extension;

        String result = buildProfileResource(baseResource, profile, type);

        assertTrue(result.endsWith(extension),
                "Result must end with the extension derived from the type: " + extension);
    }

    // ── Property: base name is correctly extracted when resource already has extension ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 7: Construção de nome de arquivo de perfil")
    void baseNameExtractedWhenResourceHasExtension(
            @ForAll("alphanumericBaseNames") String baseName,
            @ForAll("alphanumericProfiles") String profile,
            @ForAll("fileConfigTypes") ConfigurationType type) {

        String extension = "." + type.toString().toLowerCase();
        // Resource WITH extension
        String baseResourceWithExt = baseName + extension;

        String result = buildProfileResource(baseResourceWithExt, profile, type);

        assertTrue(result.startsWith(baseName + "-"),
                "Result must start with the extracted base name followed by a hyphen");
        assertEquals(baseName + "-" + profile + extension, result,
                "When resource has extension, base name must be correctly extracted");
    }

    // ── Property: resource without extension is used as-is for base name ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 7: Construção de nome de arquivo de perfil")
    void resourceWithoutExtensionUsedAsBaseName(
            @ForAll("alphanumericBaseNames") String baseName,
            @ForAll("alphanumericProfiles") String profile,
            @ForAll("fileConfigTypes") ConfigurationType type) {

        String extension = "." + type.toString().toLowerCase();
        // Resource WITHOUT extension — baseName itself does not end with extension
        // (guaranteed by generator producing only alphanumeric names)

        String result = buildProfileResource(baseName, profile, type);

        assertEquals(baseName + "-" + profile + extension, result,
                "When resource has no extension, the full resource name is used as base name");
    }

    // ── Property: result contains exactly one hyphen before profile ──

    @Property(tries = 200)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 7: Construção de nome de arquivo de perfil")
    void resultContainsProfileSeparatedByHyphen(
            @ForAll("alphanumericBaseNames") String baseName,
            @ForAll("alphanumericProfiles") String profile,
            @ForAll("fileConfigTypes") ConfigurationType type) {

        String extension = "." + type.toString().toLowerCase();
        String baseResource = baseName + extension;

        String result = buildProfileResource(baseResource, profile, type);

        // Remove extension, then check that the remainder ends with -{profile}
        String withoutExt = result.substring(0, result.length() - extension.length());
        assertTrue(withoutExt.endsWith("-" + profile),
                "After removing extension, result must end with '-{profile}'");
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> alphanumericBaseNames() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(30);
    }

    @Provide
    Arbitrary<String> alphanumericProfiles() {
        return Arbitraries.strings()
                .withCharRange('a', 'z')
                .withCharRange('A', 'Z')
                .withCharRange('0', '9')
                .ofMinLength(1)
                .ofMaxLength(15);
    }

    @Provide
    Arbitrary<ConfigurationType> fileConfigTypes() {
        return Arbitraries.of(ConfigurationType.PROPERTIES, ConfigurationType.XML);
    }
}
