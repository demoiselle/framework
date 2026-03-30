/*
 * Demoiselle Framework
 *
 * License: GNU Lesser General Public License (LGPL), version 3 or later.
 * See the lgpl.txt file in the root directory or <https://www.gnu.org/licenses/lgpl.html>.
 */
package org.demoiselle.jee.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.hamcrest.MatcherAssert.assertThat;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import jakarta.inject.Inject;

import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.annotation.ConfigurationName;
import org.demoiselle.jee.configuration.exception.DemoiselleConfigurationException;
import org.demoiselle.jee.configuration.extractor.AbstractConfigurationTest;
import org.demoiselle.jee.configuration.message.ConfigurationMessage;
import org.demoiselle.jee.configuration.model.ConfigIncompatibleTypeModel;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.model.ConfigWithNameAnnotationEmptyModel;
import org.demoiselle.jee.configuration.model.ConfigWithValidationModel;
import org.demoiselle.jee.configuration.model.ConfigWithoutExtractorModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.hamcrest.CoreMatchers;
import org.jboss.weld.junit5.auto.AddBeanClasses;
import org.jboss.weld.junit5.auto.AddExtensions;
import org.jboss.weld.junit5.auto.EnableAutoWeld;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * 
 * @author SERPRO
 *
 */
@EnableAutoWeld
@AddBeanClasses({
    ConfigurationLoader.class,
    org.demoiselle.jee.configuration.message.ConfigurationMessage.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationStringValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationPrimitiveOrWrapperValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationArrayValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationEnumValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.impl.ConfigurationClassValueExtractor.class,
    org.demoiselle.jee.configuration.extractor.ConfigurationStringValueExtractorAmbiguousTest.class,
    org.demoiselle.jee.configuration.extractor.ConfigurationStringValueExtractorAmbiguousTest2.class
})
@AddExtensions({
    ConfigurationBootstrap.class,
    org.demoiselle.jee.core.message.MessageBundleExtension.class
})
class ConfigurationLoaderTest extends AbstractConfigurationTest {

	@Inject
	private ConfigurationLoader configLoader;

	@Inject
	private ConfigurationMessage message;

	private ConfigModel configModel = new ConfigModel();

	private ClassLoader originalClassLoader;

	@BeforeEach
	void setUp() throws IOException, Exception {
		originalClassLoader = Thread.currentThread().getContextClassLoader();
		Locale.setDefault(new Locale("pt", "BR"));
		makeConfigurationRuntime(ConfigModel.class, ConfigurationType.PROPERTIES,
				utilTest.createPropertiesFile("test"));
	}

	@Override
	@AfterEach
	public void destroy() throws IOException {
		// Close the custom URLClassLoader before file cleanup to release file locks (Windows)
		if (originalClassLoader != null) {
			ClassLoader current = Thread.currentThread().getContextClassLoader();
			Thread.currentThread().setContextClassLoader(originalClassLoader);
			if (current instanceof URLClassLoader) {
				((URLClassLoader) current).close();
			}
		}
		// Now delegate to parent's file cleanup
		super.destroy();
	}

	@Test
	void shouldPopulateObject() throws IOException {

		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass);

		assertNotNull(configModel.getConfigString());

		assertEquals(UtilTest.CONFIG_STRING_VALUE, configModel.getConfigString());
		assertEquals(UtilTest.CONFIG_INTEGER_VALUE, configModel.getConfigInteger());
		assertEquals(UtilTest.CONFIG_SHORT_VALUE, configModel.getConfigShort());
		assertEquals(UtilTest.CONFIG_BOOLEAN_VALUE, configModel.getConfigBoolean());
		assertEquals(UtilTest.CONFIG_BYTE_VALUE, configModel.getConfigByte());
		assertEquals(UtilTest.CONFIG_CHARACTER_VALUE, configModel.getConfigCharacter());
		assertEquals(UtilTest.CONFIG_LONG_VALUE, configModel.getConfigLong());
		assertEquals(UtilTest.CONFIG_DOUBLE_VALUE, configModel.getConfigDouble());
		assertEquals(UtilTest.CONFIG_FLOAT_VALUE, configModel.getConfigFloat());
		assertEquals(UtilTest.CONFIG_INT_VALUE, configModel.getConfigInt());
		assertEquals(UtilTest.CONFIG_ENUM_VALUE, configModel.getConfigEnum());

		String keyIp = UtilTest.CONFIG_MAP_FIELD_IP.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		String keyPort = UtilTest.CONFIG_MAP_FIELD_PORT.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		String keyProtocol = UtilTest.CONFIG_MAP_FIELD_PROTOCOL.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");

		assertEquals(UtilTest.CONFIG_MAP_VALUE_IP, configModel.getConfigMap().get(keyIp));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PROTOCOL, configModel.getConfigMap().get(keyProtocol));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PORT, configModel.getConfigMap().get(keyPort));

		assertEquals(UtilTest.CONFIG_STRING_SUPPRESS_LOGGER_ANNOTATION_VALUE,
				configModel.getConfigFieldWithSuppressLogger());

	}

	@Test
	void shouldPopulateObjectWithNameAnnotation() {
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass);

		assertNotNull(configModel.getConfigStringWithName());
		assertEquals(UtilTest.CONFIG_STRING_NAME_ANNOTATION_VALUE, configModel.getConfigStringWithName());
	}

	@Test
	void objectWithNoMatchExtractorShouldThrowException() {
		assertThrows(DemoiselleConfigurationException.class, () -> {
			ConfigWithoutExtractorModel model = new ConfigWithoutExtractorModel();
			Class<?> baseClass = model.getClass();
			configLoader.load(model, baseClass);
		});
	}

	@Test
	void objectWithIncompatibleTypeShoutThrowConfigurationException() throws IOException, Exception {

		Properties properties = new Properties();
		properties.put("configBooleanIncompatible", "7");

		makeConfigurationRuntime(ConfigIncompatibleTypeModel.class, ConfigurationType.PROPERTIES,
				utilTest.createPropertiesFile("test", properties));

		ConfigIncompatibleTypeModel model = new ConfigIncompatibleTypeModel();

		Class<?> baseClass = model.getClass();

		assertThrows(DemoiselleConfigurationException.class, () -> {
			configLoader.load(model, baseClass);
		});
	}

	@Test
	void fieldWithIgnoreAnnotationShouldntLoad() {
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigFieldWithIgnore());
		configLoader.load(configModel, baseClass);
		assertNull(configModel.getConfigFieldWithIgnore());
	}

	@Test
	void modelInvalidValuesWithBeanValidationShouldThrowConfigurationException() {
		assertThrows(DemoiselleConfigurationException.class, () -> {
			ConfigWithValidationModel model = new ConfigWithValidationModel();
			Class<?> baseClass = model.getClass();
			configLoader.load(model, baseClass);
		});
	}

	@Test
	void modelInvalidValueWithBeanValidationShouldShowError()
			throws NoSuchFieldException, IllegalAccessException {

		StringBuilder sb = new StringBuilder();
		sb.append(
				"private java.lang.String org.demoiselle.jee.configuration.model.ConfigWithValidationModel.configString não deve ser nulo\n");

		try {
			ConfigWithValidationModel model = new ConfigWithValidationModel();
			Class<?> baseClass = model.getClass();
			configLoader.load(model, baseClass);
		} catch (DemoiselleConfigurationException e) {
			assertNotNull(e.getMessage());
			assertEquals(sb.toString(), e.getMessage());
		}

	}

	@Test
	void twoExtractorValueThatSupportTheSameTypeShouldThrowConfigurationException()
			throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {

		try {
			Class<?> baseClass = configModel.getClass();
			assertNull(configModel.getConfigString());
			configLoader.load(configModel, baseClass);
		} catch (DemoiselleConfigurationException e) {
			assertNotNull(e.getMessage());
			assertThat(e.getMessage(), CoreMatchers.containsString(
					"Foi detectada ambiguidade da interface org.demoiselle.jee.configuration.extractor.ConfigurationValueExtractor com as seguintes implementações:"));
			assertThat(e.getMessage(), CoreMatchers.containsString(
					"Para resolver o conflito, defina explicitamente a implementação no demoiselle.properties."));
		}
	}

	@Test
	void configurationWithInvalidResourceShouldThrowConfigurationException() throws IOException, Exception {

		makeConfigurationRuntime(ConfigModel.class, ConfigurationType.PROPERTIES, "file-not-found");

		Class<?> baseClass = configModel.getClass();

		try {
			configLoader.load(configModel, baseClass);
		} catch (DemoiselleConfigurationException e) {
			assertNotNull(e.getMessage());
			assertEquals(e.getMessage(), "O arquivo file-not-found.properties não foi encontrado");
		}

	}

	@Test
	void modelWithEmptyNameAnnotationShouldThrowException() {
		ConfigWithNameAnnotationEmptyModel model = new ConfigWithNameAnnotationEmptyModel();
		Class<?> baseClass = model.getClass();

		try {
			configLoader.load(model, baseClass);
		} catch (DemoiselleConfigurationException e) {
			assertNotNull(e.getMessage());
			assertEquals(e.getMessage(), message.configurationNameAttributeCantBeEmpty(ConfigurationName.class.getSimpleName()));
		}
	}

	@Test
	void configurationLoaderShouldLoadConfigurationXML() throws Exception {
		makeConfigurationRuntime(ConfigModel.class, ConfigurationType.XML, utilTest.createXMLFile("test"));

		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass);

		assertNotNull(configModel.getConfigString());
		assertEquals(UtilTest.CONFIG_STRING_VALUE, configModel.getConfigString());
	}

	@Test
	void configurationLoaderShouldLoadConfigurationSystem() throws Exception {
		utilTest.createSystemVariables();
		makeConfigurationRuntime(ConfigModel.class, ConfigurationType.SYSTEM, null);

		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass);

		assertNotNull(configModel.getConfigString());
		assertEquals(UtilTest.CONFIG_STRING_VALUE, configModel.getConfigString());
	}

	private void makeConfigurationRuntime(Class<?> clazz, ConfigurationType configType, String pathFileTest)
			throws Exception {
		final Configuration oldConfiguration = clazz.getDeclaredAnnotation(Configuration.class);

		Configuration configuration = new Configuration() {

			@Override
			public Class<? extends Annotation> annotationType() {
				return oldConfiguration.annotationType();
			}

			@Override
			public ConfigurationType type() {
				return configType;
			}

			@Override
			public String resource() {

				if (configType.equals(ConfigurationType.SYSTEM)) {
					return null;
				}

				String replacePattern = ".properties";

				if (configType.equals(ConfigurationType.XML)) {
					replacePattern = ".xml";
				}

				return new File(pathFileTest).getName().replaceAll(replacePattern, "");
			}

			@Override
			public String prefix() {
				return "";
			}
		};

		Field annotationDataField = Class.class.getDeclaredField("annotationData");
		annotationDataField.setAccessible(true);

		Object object = annotationDataField.get(clazz);

		Field field = object.getClass().getDeclaredField("annotations");
		field.setAccessible(true);

		@SuppressWarnings("unchecked")
		Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) field
				.get(object);

		annotations.put(Configuration.class, configuration);

		// Close any previously created URLClassLoader before creating a new one
		ClassLoader current = Thread.currentThread().getContextClassLoader();
		if (current instanceof URLClassLoader && current != originalClassLoader) {
			((URLClassLoader) current).close();
			Thread.currentThread().setContextClassLoader(originalClassLoader);
		}

		URLClassLoader ucl = new URLClassLoader(
				new URL[]{utilTest.getDirectoryTemp().toUri().toURL()},
				Thread.currentThread().getContextClassLoader());
		Thread.currentThread().setContextClassLoader(ucl);

	}

}
