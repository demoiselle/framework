/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.jqwik.api.*;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 8: Precedência de valores perfil sobre padrão
 *
 * <p><b>Validates: Requirements 5.3</b></p>
 *
 * For any configuration key present in both the profile and default configuration
 * sources, the value returned must be the profile value. For keys present only in
 * the default configuration, the default value must be returned.
 *
 * <p>This test simulates the precedence mechanism used by
 * {@code ConfigurationLoader.getValueFromSource()}: configurations are stored in
 * a list where the profile configuration is added first, followed by the default.
 * The extraction loop iterates through the list and takes the first non-null value,
 * giving natural precedence to the profile configuration.</p>
 */
class ProfileValuePrecedencePropertyTest {

    /**
     * Replicates the value resolution logic from
     * {@code ConfigurationLoader.getValueFromSource()}:
     * <pre>
     *   for (Configuration config : configurations) {
     *       if (value != null) break;
     *       value = config.getString(key, null);
     *   }
     * </pre>
     *
     * @param key            the configuration key to look up
     * @param configurations ordered list of configurations (profile first, default second)
     * @return the first non-null value found, or null if none
     */
    private static String resolveValue(String key, List<Configuration> configurations) {
        String value = null;
        for (Configuration config : configurations) {
            if (value != null) {
                break;
            }
            value = config.getString(key, null);
        }
        return value;
    }

    /**
     * Builds an ordered configuration list with profile first, default second.
     */
    private static List<Configuration> buildConfigurationList(
            Map<String, String> profileEntries,
            Map<String, String> defaultEntries) {

        BaseConfiguration profileConfig = new BaseConfiguration();
        profileEntries.forEach(profileConfig::setProperty);

        BaseConfiguration defaultConfig = new BaseConfiguration();
        defaultEntries.forEach(defaultConfig::setProperty);

        List<Configuration> configs = new ArrayList<>();
        configs.add(profileConfig);  // profile first — higher precedence
        configs.add(defaultConfig);  // default second — lower precedence
        return configs;
    }

    // ── Property: profile values override default values for overlapping keys ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 8: Precedência de valores perfil sobre padrão")
    void profileValueOverridesDefaultForOverlappingKeys(
            @ForAll("configKeys") List<String> overlappingKeys,
            @ForAll("configValues") List<String> profileValues,
            @ForAll("configValues") List<String> defaultValues) {

        // Ensure we have at least one overlapping key
        Assume.that(!overlappingKeys.isEmpty());

        int size = Math.min(overlappingKeys.size(),
                Math.min(profileValues.size(), defaultValues.size()));

        Map<String, String> profileEntries = new HashMap<>();
        Map<String, String> defaultEntries = new HashMap<>();

        for (int i = 0; i < size; i++) {
            String key = overlappingKeys.get(i);
            profileEntries.put(key, profileValues.get(i));
            defaultEntries.put(key, defaultValues.get(i));
        }

        List<Configuration> configs = buildConfigurationList(profileEntries, defaultEntries);

        for (int i = 0; i < size; i++) {
            String key = overlappingKeys.get(i);
            String resolved = resolveValue(key, configs);
            assertEquals(profileEntries.get(key), resolved,
                    "For overlapping key '" + key + "', profile value must prevail over default");
        }
    }

    // ── Property: default-only keys are still accessible ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 8: Precedência de valores perfil sobre padrão")
    void defaultOnlyKeysAreAccessible(
            @ForAll("configKeys") List<String> defaultOnlyKeys,
            @ForAll("configValues") List<String> defaultValues) {

        Assume.that(!defaultOnlyKeys.isEmpty());

        int size = Math.min(defaultOnlyKeys.size(), defaultValues.size());

        Map<String, String> profileEntries = new HashMap<>(); // empty profile
        Map<String, String> defaultEntries = new HashMap<>();

        for (int i = 0; i < size; i++) {
            defaultEntries.put(defaultOnlyKeys.get(i), defaultValues.get(i));
        }

        List<Configuration> configs = buildConfigurationList(profileEntries, defaultEntries);

        for (int i = 0; i < size; i++) {
            String key = defaultOnlyKeys.get(i);
            String resolved = resolveValue(key, configs);
            assertEquals(defaultEntries.get(key), resolved,
                    "For default-only key '" + key + "', default value must be returned");
        }
    }

    // ── Property: profile-only keys are resolved from profile ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 8: Precedência de valores perfil sobre padrão")
    void profileOnlyKeysAreResolvedFromProfile(
            @ForAll("configKeys") List<String> profileOnlyKeys,
            @ForAll("configValues") List<String> profileValues) {

        Assume.that(!profileOnlyKeys.isEmpty());

        int size = Math.min(profileOnlyKeys.size(), profileValues.size());

        Map<String, String> profileEntries = new HashMap<>();
        Map<String, String> defaultEntries = new HashMap<>(); // empty default

        for (int i = 0; i < size; i++) {
            profileEntries.put(profileOnlyKeys.get(i), profileValues.get(i));
        }

        List<Configuration> configs = buildConfigurationList(profileEntries, defaultEntries);

        for (int i = 0; i < size; i++) {
            String key = profileOnlyKeys.get(i);
            String resolved = resolveValue(key, configs);
            assertEquals(profileEntries.get(key), resolved,
                    "For profile-only key '" + key + "', profile value must be returned");
        }
    }

    // ── Property: keys absent from both return null ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 8: Precedência de valores perfil sobre padrão")
    void keysAbsentFromBothReturnNull(
            @ForAll("configKeys") List<String> populatedKeys,
            @ForAll("configValues") List<String> values,
            @ForAll("configKeys") List<String> absentKeys) {

        Assume.that(!absentKeys.isEmpty());

        int size = Math.min(populatedKeys.size(), values.size());

        Map<String, String> profileEntries = new HashMap<>();
        Map<String, String> defaultEntries = new HashMap<>();

        for (int i = 0; i < size; i++) {
            profileEntries.put(populatedKeys.get(i), values.get(i));
            defaultEntries.put(populatedKeys.get(i), values.get(i));
        }

        List<Configuration> configs = buildConfigurationList(profileEntries, defaultEntries);

        for (String absentKey : absentKeys) {
            if (!profileEntries.containsKey(absentKey) && !defaultEntries.containsKey(absentKey)) {
                String resolved = resolveValue(absentKey, configs);
                assertNull(resolved,
                        "Key '" + absentKey + "' absent from both configs must resolve to null");
            }
        }
    }

    // ── Property: mixed scenario — overlapping + default-only keys ──

    @Property(tries = 100)
    @Tag("Feature: configuration-enhancements")
    @Tag("Property 8: Precedência de valores perfil sobre padrão")
    void mixedOverlappingAndDefaultOnlyKeys(
            @ForAll("configKeys") List<String> overlappingKeys,
            @ForAll("configKeys") List<String> defaultOnlyKeys,
            @ForAll("configValues") List<String> profileValues,
            @ForAll("configValues") List<String> defaultOverlapValues,
            @ForAll("configValues") List<String> defaultOnlyValues) {

        int overlapSize = Math.min(overlappingKeys.size(),
                Math.min(profileValues.size(), defaultOverlapValues.size()));
        int defaultOnlySize = Math.min(defaultOnlyKeys.size(), defaultOnlyValues.size());

        Assume.that(overlapSize > 0 || defaultOnlySize > 0);

        Map<String, String> profileEntries = new HashMap<>();
        Map<String, String> defaultEntries = new HashMap<>();

        // Add overlapping keys to both
        for (int i = 0; i < overlapSize; i++) {
            String key = overlappingKeys.get(i);
            profileEntries.put(key, profileValues.get(i));
            defaultEntries.put(key, defaultOverlapValues.get(i));
        }

        // Add default-only keys (ensure they don't overlap with profile keys)
        for (int i = 0; i < defaultOnlySize; i++) {
            String key = defaultOnlyKeys.get(i);
            if (!profileEntries.containsKey(key)) {
                defaultEntries.put(key, defaultOnlyValues.get(i));
            }
        }

        List<Configuration> configs = buildConfigurationList(profileEntries, defaultEntries);

        // Verify overlapping keys use profile value
        for (int i = 0; i < overlapSize; i++) {
            String key = overlappingKeys.get(i);
            String resolved = resolveValue(key, configs);
            assertEquals(profileEntries.get(key), resolved,
                    "Overlapping key '" + key + "' must use profile value");
        }

        // Verify default-only keys use default value
        for (Map.Entry<String, String> entry : defaultEntries.entrySet()) {
            if (!profileEntries.containsKey(entry.getKey())) {
                String resolved = resolveValue(entry.getKey(), configs);
                assertEquals(entry.getValue(), resolved,
                        "Default-only key '" + entry.getKey() + "' must use default value");
            }
        }
    }

    // ── Generators ──

    @Provide
    Arbitrary<List<String>> configKeys() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(20)
                .map(String::toLowerCase)
                .list()
                .ofMinSize(1)
                .ofMaxSize(10)
                .uniqueElements();
    }

    @Provide
    Arbitrary<List<String>> configValues() {
        return Arbitraries.strings()
                .ofMinLength(1)
                .ofMaxLength(50)
                .list()
                .ofMinSize(1)
                .ofMaxSize(10);
    }
}
