/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationArrayValueExtractor;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.Test;

/**
 * 
 * @author SERPRO
 *
 */
public class ArrayExtractorTest extends AbstractConfigurationTest {

    private ConfigurationArrayValueExtractor conf = new ConfigurationArrayValueExtractor();
    private ConfigModel configModel = new ConfigModel();

    /**
     * Test whether {@link ConfigurationArrayValueExtractor} can extract values
     * from a PROPERTIES file
     * 
     * @throws Exception
     */
    @Test
    public void extractArrayFromProperties() throws Exception {
        Configuration configuration = utilTest.buildConfiguration(PropertiesConfiguration.class,
                utilTest.createPropertiesFile(FILE_PREFIX));

        Field field = configModel.getClass().getDeclaredField(UtilTest.CONFIG_ARRAY_FIELD);

        String[] result = (String[]) conf.getValue(PREFIX, UtilTest.CONFIG_ARRAY_FIELD, field, configuration);

        List<String> list = Arrays.asList(result);

        assertEquals(UtilTest.CONFIG_ARRAY_VALUE.getClass(), result.getClass());
        assertEquals(3, result.length);
        assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_1));
        assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_2));
        assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_3));
    }

    /**
     * Test whether {@link ConfigurationArrayValueExtractor} can extract values
     * from a XML file
     * 
     * @throws Exception
     */
    @Test
    public void extractArrayFromXML() throws Exception {
        Configuration configuration = utilTest.buildConfiguration(XMLConfiguration.class,
                utilTest.createXMLFile(FILE_PREFIX));

        Field field = configModel.getClass().getDeclaredField(UtilTest.CONFIG_ARRAY_FIELD);

        String[] result = (String[]) conf.getValue(PREFIX, UtilTest.CONFIG_ARRAY_FIELD, field, configuration);

        List<String> list = Arrays.asList(result);

        assertEquals(UtilTest.CONFIG_ARRAY_VALUE.getClass(), result.getClass());
        assertEquals(3, result.length);
        assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_1));
        assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_2));
        assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_3));
    }

    /*
     * System properties doesn't accept duplicate key, so I need test Array
     * Extractor.
     * 
     * @Test
     * public void extractArrayFromSystem() throws Exception{
     * utilTest.createSystemVariables();
     * 
     * BasicConfigurationBuilder<? extends Configuration> builder = new
     * BasicConfigurationBuilder<>(SystemConfiguration.class);
     * 
     * Field field =
     * configModel.getClass().getDeclaredField(UtilTest.CONFIG_ARRAY_FIELD);
     * 
     * String[] result = (String[]) conf.getValue(PREFIX,
     * UtilTest.CONFIG_ARRAY_FIELD, field, builder.getConfiguration());
     * 
     * List<String> list = Arrays.asList(result);
     * 
     * assertEquals(UtilTest.CONFIG_ARRAY_VALUE.getClass(), result.getClass());
     * assertEquals(3, result.length);
     * assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_1));
     * assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_2));
     * assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_3));
     * }
     */

    /**
     * Test whether {@link ConfigurationArrayValueExtractor} can support array
     * type
     * 
     * @throws Exception
     */
    @Test
    public void extractorShouldBeSupportArray() throws NoSuchFieldException, SecurityException {
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_ARRAY_FIELD)));
    }

}
