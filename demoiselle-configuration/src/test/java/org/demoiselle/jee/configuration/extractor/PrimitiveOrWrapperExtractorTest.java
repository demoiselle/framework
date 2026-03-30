/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author SERPRO
 *
 */
class PrimitiveOrWrapperExtractorTest extends AbstractConfigurationTest {

    private ConfigModel configModel = new ConfigModel();
    private ConfigurationValueExtractor conf = new ConfigurationPrimitiveOrWrapperValueExtractor();

    @Test
    void extractorShouldBeSupportInteger() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_INTEGER_FIELD)));
    }

    @Test
    void extractorShouldBeSupportShort() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_SHORT_FIELD)));
    }

    @Test
    void extractorShouldBeSupportBoolean() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_BOOLEAN_FIELD)));
    }

    @Test
    void extractorShouldBeSupportByte() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_BYTE_FIELD)));
    }

    @Test
    void extractorShouldBeSupportCharacter() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_CHARACTER_FIELD)));
    }

    @Test
    void extractorShouldBeSupportLong() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_LONG_FIELD)));
    }

    @Test
    void extractorShouldBeSupportDuble() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_DOUBLE_FIELD)));
    }

    @Test
    void extractorShouldBeSupportFloat() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_FLOAT_FIELD)));
    }

    @Test
    void extractorShouldNotBeSupportString() throws NoSuchFieldException, SecurityException {
        assertFalse(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_STRING_FIELD)));
    }

    @Test
    void extractorShouldBeSupportInt() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_INT_FIELD)));
    }

    // ---------- PROPERTIES

    @Test
    void extractIntegerFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_INTEGER_FIELD, UtilTest.CONFIG_INTEGER_VALUE);
    }

    @Test
    void extractShortFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_SHORT_FIELD, UtilTest.CONFIG_SHORT_VALUE);
    }

    @Test
    void extractBooleanFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_BOOLEAN_FIELD, UtilTest.CONFIG_BOOLEAN_VALUE);
    }

    @Test
    void extractByteFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_BYTE_FIELD, UtilTest.CONFIG_BYTE_VALUE);
    }

    @Test
    void extractCharacterFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_CHARACTER_FIELD, UtilTest.CONFIG_CHARACTER_VALUE);
    }

    @Test
    void extractLongFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_LONG_FIELD, UtilTest.CONFIG_LONG_VALUE);
    }

    @Test
    void extractDoubleFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_DOUBLE_FIELD, UtilTest.CONFIG_DOUBLE_VALUE);
    }

    @Test
    void extractFloatFromProperties() throws Exception {
        testValueFromProperties(UtilTest.CONFIG_FLOAT_FIELD, UtilTest.CONFIG_FLOAT_VALUE);
    }

    // ---------- XML

    @Test
    void extractIntegerFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_INTEGER_FIELD, UtilTest.CONFIG_INTEGER_VALUE);
    }

    @Test
    void extractShortFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_SHORT_FIELD, UtilTest.CONFIG_SHORT_VALUE);
    }

    @Test
    void extractBooleanFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_BOOLEAN_FIELD, UtilTest.CONFIG_BOOLEAN_VALUE);
    }

    @Test
    void extractByteFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_BYTE_FIELD, UtilTest.CONFIG_BYTE_VALUE);
    }

    @Test
    void extractCharacterFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_CHARACTER_FIELD, UtilTest.CONFIG_CHARACTER_VALUE);
    }

    @Test
    void extractLongFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_LONG_FIELD, UtilTest.CONFIG_LONG_VALUE);
    }

    @Test
    void extractDoubleFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_DOUBLE_FIELD, UtilTest.CONFIG_DOUBLE_VALUE);
    }

    @Test
    void extractFloatFromXML() throws Exception {
        testValueFromXML(UtilTest.CONFIG_FLOAT_FIELD, UtilTest.CONFIG_FLOAT_VALUE);
    }

    // ---------- SYSTEM

    @Test
    void extractIntegerFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_INTEGER_FIELD, UtilTest.CONFIG_INTEGER_VALUE);
    }

    @Test
    void extractShortFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_SHORT_FIELD, UtilTest.CONFIG_SHORT_VALUE);
    }

    @Test
    void extractBooleanFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_BOOLEAN_FIELD, UtilTest.CONFIG_BOOLEAN_VALUE);
    }

    @Test
    void extractByteFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_BYTE_FIELD, UtilTest.CONFIG_BYTE_VALUE);
    }

    @Test
    void extractCharacterFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_CHARACTER_FIELD, UtilTest.CONFIG_CHARACTER_VALUE);
    }

    @Test
    void extractLongFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_LONG_FIELD, UtilTest.CONFIG_LONG_VALUE);
    }

    @Test
    void extractDoubleFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_DOUBLE_FIELD, UtilTest.CONFIG_DOUBLE_VALUE);
    }

    @Test
    void extractFloatFromSystem() throws Exception {
        testValueFromSystem(UtilTest.CONFIG_FLOAT_FIELD, UtilTest.CONFIG_FLOAT_VALUE);
    }

    private void testValueFromSystem(String key, Object value) throws ConfigurationException, Exception {
        utilTest.createSystemVariables();
        BasicConfigurationBuilder<? extends Configuration> builder = new BasicConfigurationBuilder<>(
                SystemConfiguration.class);

        Field field = configModel.getClass().getDeclaredField(key);
        Object result = conf.getValue(PREFIX, key, field, builder.getConfiguration());

        assertEquals(value.getClass(), result.getClass());
        assertEquals(value, result);
    }

    private void testValueFromProperties(String key, Object value) throws Exception {
        Configuration configuration = utilTest.buildConfiguration(PropertiesConfiguration.class,
                utilTest.createPropertiesFile(FILE_PREFIX));

        Field field = configModel.getClass().getDeclaredField(key);
        Object result = conf.getValue(PREFIX, key, field, configuration);

        assertEquals(value.getClass(), result.getClass());
        assertEquals(value, result);
    }

    private void testValueFromXML(String key, Object value) throws Exception {
        Configuration configuration = utilTest.buildConfiguration(XMLConfiguration.class,
                utilTest.createXMLFile(FILE_PREFIX));

        Field field = configModel.getClass().getDeclaredField(key);
        Object result = conf.getValue(PREFIX, key, field, configuration);

        assertEquals(value.getClass(), result.getClass());
        assertEquals(value, result);
    }

}
