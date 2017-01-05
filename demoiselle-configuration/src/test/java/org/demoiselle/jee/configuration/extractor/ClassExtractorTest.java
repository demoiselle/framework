/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration.extractor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.Method;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.StringUtils;
import org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationClassValueExtractor;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.Test;

/**
 * 
 * @author SERPRO
 *
 */
public class ClassExtractorTest extends AbstractConfigurationTest {

    private ConfigModel configModel = new ConfigModel();

    private ConfigurationValueExtractor conf = new ConfigurationClassValueExtractor();

    @Test
    public void extractClassFromProperties() throws Exception {
        Configuration configuration = utilTest.buildConfiguration(PropertiesConfiguration.class,
                utilTest.createPropertiesFile(FILE_PREFIX));

        Object value = conf.getValue(PREFIX, UtilTest.CONFIG_CLASS_TYPED_FIELD,
                configModel.getClass().getDeclaredField(UtilTest.CONFIG_CLASS_TYPED_FIELD), configuration);

        assertEquals(value.getClass(), Class.class);
        assertEquals(value, UtilTest.CONFIG_CLASS_TYPED_VALUE);
    }

    @Test
    public void extractClassFromXML() throws Exception {
        Configuration configuration = utilTest.buildConfiguration(XMLConfiguration.class,
                utilTest.createXMLFile(FILE_PREFIX));

        Object value = conf.getValue(PREFIX, UtilTest.CONFIG_CLASS_TYPED_FIELD,
                configModel.getClass().getDeclaredField(UtilTest.CONFIG_CLASS_TYPED_FIELD), configuration);

        assertEquals(value.getClass(), Class.class);
        assertEquals(value, UtilTest.CONFIG_CLASS_TYPED_VALUE);
    }

    @Test
    public void extractClassFromSystemVariable() throws ConfigurationException, Exception {
        utilTest.createSystemVariables();
        BasicConfigurationBuilder<? extends Configuration> builder = new BasicConfigurationBuilder<>(
                SystemConfiguration.class);

        Object value = conf.getValue(PREFIX, UtilTest.CONFIG_CLASS_TYPED_FIELD,
                configModel.getClass().getDeclaredField(UtilTest.CONFIG_CLASS_TYPED_FIELD), builder.getConfiguration());

        assertEquals(value.getClass(), Class.class);
        assertEquals(value, UtilTest.CONFIG_CLASS_TYPED_VALUE);
    }

    @Test
    public void extractClassShouldBeSupportString()
            throws NoSuchFieldException, SecurityException, NoSuchMethodException {
        final Method method = configModel.getClass()
                .getDeclaredMethod("get" + StringUtils.capitalize(UtilTest.CONFIG_CLASS_TYPED_FIELD));

        assertEquals(method.getReturnType(), Class.class);
        assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_CLASS_TYPED_FIELD)));
    }

}
