/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.pbt;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.jqwik.api.*;
import net.jqwik.api.constraints.Size;

import org.apache.commons.configuration2.BaseConfiguration;
import org.apache.commons.configuration2.Configuration;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Feature: configuration-enhancements, Property 2: Extração de Map com cache de Pattern mantém equivalência funcional
 *
 * <p><b>Validates: Requirements 3.4</b></p>
 *
 * For any combination of prefix, key, and Configuration entries, the result
 * returned by {@link ConfigurationMapValueExtractor#getValue} with the Pattern
 * cache must be identical to the result produced by compiling the Pattern fresh
 * each time.
 */
class MapExtractorPatternCachePropertyTest {

    /** Dummy class that holds a Map field for the extractor to inspect. */
    static class MapHolder {
        Map<String, String> data;
    }

    private static final Field MAP_FIELD;

    static {
        try {
            MAP_FIELD = MapHolder.class.getDeclaredField("data");
        } catch (NoSuchFieldException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    // ── Reference implementation: compiles Pattern fresh each time ──

    /**
     * Mirrors the extraction logic of ConfigurationMapValueExtractor.getValue()
     * but compiles a brand-new Pattern on every call (no cache).
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> extractWithFreshPattern(
            String prefix, String key, Configuration configuration) {

        Map<String, Object> value = null;

        String regexp = "^(" + prefix + ")(" + key + ")(\\.([^=]*))?$";
        Pattern pattern = Pattern.compile(regexp); // fresh compile every time

        for (Iterator<String> iter = configuration.getKeys(); iter.hasNext(); ) {
            String iterKey = iter.next();
            Matcher matcher = pattern.matcher(iterKey);

            if (matcher.matches()) {
                String confKey = matcher.group(1) + matcher.group(2)
                        + (matcher.group(3) != null ? matcher.group(3) : "");

                if (value == null) {
                    value = new HashMap<>();
                }

                String mapKey = matcher.group(4) == null ? "default" : matcher.group(4);
                value.putIfAbsent(mapKey, configuration.getString(confKey));
            }
        }

        return value;
    }

    // ── Generators ──

    @Provide
    Arbitrary<String> prefixes() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(10)
                .map(s -> s.toLowerCase() + "\\.");
    }

    @Provide
    Arbitrary<String> keys() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(15)
                .map(String::toLowerCase);
    }

    @Provide
    Arbitrary<String> mapSubKeys() {
        return Arbitraries.strings()
                .alpha()
                .ofMinLength(1)
                .ofMaxLength(8)
                .map(String::toLowerCase);
    }

    @Provide
    Arbitrary<String> configValues() {
        return Arbitraries.strings()
                .ascii()
                .ofMinLength(0)
                .ofMaxLength(50);
    }

    // ── Property test ──

    @Property(tries = 100)
    void cachedPatternProducesSameResultAsFreshPattern(
            @ForAll("prefixes") String prefix,
            @ForAll("keys") String key,
            @ForAll @Size(min = 0, max = 5) java.util.List<
                    @From("mapSubKeys") String> subKeys,
            @ForAll("configValues") String sampleValue) throws Exception {

        // Build the raw prefix (without regex escape) for configuration keys
        // prefix generator produces e.g. "abc\\." — the literal prefix is "abc."
        String literalPrefix = prefix.replace("\\.", ".");

        // Build a Configuration with matching and non-matching entries
        BaseConfiguration configuration = new BaseConfiguration();

        // Add matching entries: prefix + key + ".subKey"
        for (String subKey : subKeys) {
            String fullKey = literalPrefix + key + "." + subKey;
            configuration.setProperty(fullKey, sampleValue + "_" + subKey);
        }

        // Also add a bare entry (no sub-key): prefix + key
        configuration.setProperty(literalPrefix + key, sampleValue);

        // Add a non-matching entry to ensure it's filtered out
        configuration.setProperty("unrelated.noise.entry", "noise");

        // Extract using the cached extractor (production code)
        ConfigurationMapValueExtractor cachedExtractor = new ConfigurationMapValueExtractor();
        @SuppressWarnings("unchecked")
        Map<String, Object> cachedResult = (Map<String, Object>) cachedExtractor.getValue(
                prefix, key, MAP_FIELD, configuration);

        // Extract using fresh Pattern compilation (reference implementation)
        Map<String, Object> freshResult = extractWithFreshPattern(prefix, key, configuration);

        // Both must be identical
        assertEquals(freshResult, cachedResult,
                "Cached Pattern extraction must match fresh Pattern extraction");
    }

    @Property(tries = 100)
    void repeatedCallsWithSamePrefixAndKeyProduceSameResult(
            @ForAll("prefixes") String prefix,
            @ForAll("keys") String key,
            @ForAll @Size(min = 1, max = 3) java.util.List<
                    @From("mapSubKeys") String> subKeys,
            @ForAll("configValues") String sampleValue) throws Exception {

        String literalPrefix = prefix.replace("\\.", ".");

        BaseConfiguration configuration = new BaseConfiguration();
        for (String subKey : subKeys) {
            configuration.setProperty(literalPrefix + key + "." + subKey, sampleValue);
        }

        // Use the same extractor instance (same cache) for two calls
        ConfigurationMapValueExtractor extractor = new ConfigurationMapValueExtractor();

        @SuppressWarnings("unchecked")
        Map<String, Object> firstCall = (Map<String, Object>) extractor.getValue(
                prefix, key, MAP_FIELD, configuration);

        @SuppressWarnings("unchecked")
        Map<String, Object> secondCall = (Map<String, Object>) extractor.getValue(
                prefix, key, MAP_FIELD, configuration);

        assertEquals(firstCall, secondCall,
                "Repeated calls with same parameters must return identical results");
    }
}
