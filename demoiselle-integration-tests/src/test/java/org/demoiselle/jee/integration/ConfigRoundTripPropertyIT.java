/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.integration;

// Feature: cross-cutting-improvements, Property 7: Round-trip de configuração

import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.constraints.AlphaChars;
import net.jqwik.api.constraints.IntRange;
import net.jqwik.api.constraints.StringLength;

import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.builder.FileBasedConfigurationBuilder;
import org.apache.commons.configuration2.builder.fluent.Parameters;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Property-based integration test: Configuration round-trip.
 *
 * <p>For any valid set of randomly generated configuration properties,
 * writing them to a properties file and loading them via the same mechanism
 * used by {@code ConfigurationLoader} (Apache Commons Configuration +
 * reflection-based field injection) produces values equivalent to the
 * originals.</p>
 *
 * <p><b>Validates: Requirements 11.1</b></p>
 */
class ConfigRoundTripPropertyIT {

    /**
     * Simple POJO that mirrors a {@code @Configuration} class with
     * String, int, and boolean fields — the types ConfigurationLoader
     * supports via its built-in extractors.
     */
    static class RoundTripConfig {
        private String stringProp;
        private int intProp;
        private boolean boolProp;

        String getStringProp() { return stringProp; }
        int getIntProp() { return intProp; }
        boolean isBoolProp() { return boolProp; }
    }

    /**
     * <b>Property 7: Round-trip de configuração</b>
     *
     * <p>For any valid set of generated properties (String, int, boolean),
     * writing to a temporary properties file, loading via
     * {@code PropertiesConfiguration} (the same engine used by
     * {@code ConfigurationLoader}), and injecting values into fields via
     * reflection produces values equivalent to the originals.</p>
     *
     * <p><b>Validates: Requirements 11.1</b></p>
     */
    @Property(tries = 100)
    void roundTripConfigurationPreservesValues(
            @ForAll @AlphaChars @StringLength(min = 1, max = 80) String stringVal,
            @ForAll @IntRange(min = Integer.MIN_VALUE, max = Integer.MAX_VALUE) int intVal,
            @ForAll boolean boolVal
    ) throws Exception {

        Path tempDir = Files.createTempDirectory("config-roundtrip-");
        File propsFile = tempDir.resolve("roundtrip.properties").toFile();

        try {
            // 1. Write generated properties to a temporary file
            writePropertiesFile(propsFile, stringVal, intVal, boolVal);

            // 2. Load via Apache Commons PropertiesConfiguration
            //    (same mechanism ConfigurationLoader uses internally)
            PropertiesConfiguration config = loadConfiguration(propsFile);

            // 3. Simulate ConfigurationLoader field injection via reflection
            RoundTripConfig target = new RoundTripConfig();
            injectField(target, "stringProp", config.getString("stringProp"));
            injectField(target, "intProp", config.getInt("intProp"));
            injectField(target, "boolProp", config.getBoolean("boolProp"));

            // 4. Verify round-trip: loaded values must equal originals
            assertEquals(stringVal, target.getStringProp(),
                    "String property round-trip failed");
            assertEquals(intVal, target.getIntProp(),
                    "int property round-trip failed");
            assertEquals(boolVal, target.isBoolProp(),
                    "boolean property round-trip failed");

        } finally {
            // Cleanup
            propsFile.delete();
            tempDir.toFile().delete();
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private void writePropertiesFile(File file, String stringVal, int intVal, boolean boolVal)
            throws IOException {
        StringBuilder sb = new StringBuilder();
        sb.append("stringProp=").append(stringVal).append('\n');
        sb.append("intProp=").append(intVal).append('\n');
        sb.append("boolProp=").append(boolVal).append('\n');

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(sb.toString().getBytes());
        }
    }

    private PropertiesConfiguration loadConfiguration(File file)
            throws ConfigurationException, MalformedURLException {
        Parameters params = new Parameters();
        FileBasedConfigurationBuilder<PropertiesConfiguration> builder =
                new FileBasedConfigurationBuilder<>(PropertiesConfiguration.class);
        builder.configure(params.fileBased().setURL(file.toURI().toURL()));
        return builder.getConfiguration();
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(fieldName);
        f.setAccessible(true);
        f.set(target, value);
    }
}
