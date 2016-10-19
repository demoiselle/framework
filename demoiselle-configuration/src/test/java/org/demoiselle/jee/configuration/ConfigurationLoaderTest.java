package org.demoiselle.jee.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Map;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.demoiselle.jee.configuration.annotation.Configuration;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.model.ConfigWithoutExtractorModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(CdiTestRunner.class)
public class ConfigurationLoaderTest {
	
	@Inject
	private ConfigurationLoader configLoader;
	
	private ConfigModel configModel;
	
	private static UtilTest utilTest = new UtilTest();
	private String pathFileTest = "";
	
	public ConfigurationLoaderTest() throws Exception {
		pathFileTest = utilTest.createPropertiesFile("test");
		
		configModel = new ConfigModel();
		
		makeConfigurationRuntime();
	}
	
	private void makeConfigurationRuntime() throws Exception{
		final Configuration oldConfiguration = ConfigModel.class.getDeclaredAnnotation(Configuration.class);
		
		Configuration configuration = new Configuration() {
			
			@Override
			public Class<? extends Annotation> annotationType() {
				return oldConfiguration.annotationType();
			}
			
			@Override
			public ConfigType type() { 
				return ConfigType.PROPERTIES;
			}
			
			@Override
			public String resource() {
				return new File(pathFileTest).getName().replaceAll(".properties", "");
			}
			
			@Override
			public String prefix() {
 				return "";
			}
		};
		
		Field annotationDataField = Class.class.getDeclaredField("annotationData");
		annotationDataField.setAccessible(true);
		
		Object object = annotationDataField.get(ConfigModel.class);
		
		Field field = object.getClass().getDeclaredField("annotations");
		field.setAccessible(true);
		
		@SuppressWarnings("unchecked")
		Map<Class<? extends Annotation>, Annotation> annotations = (Map<Class<? extends Annotation>, Annotation>) field.get(object);
		
		annotations.put(Configuration.class, configuration);
		
		URLClassLoader ucl = (URLClassLoader) Thread.currentThread().getContextClassLoader();
		Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
		method.setAccessible(true);
		
		method.invoke(ucl, utilTest.getDirectoryTemp().toUri().toURL());
		
	}

	@After
	public void destroy() throws IOException{
		utilTest.deleteFilesAfterTest();
	}
	
	@Test
	public void shouldPopulateObject() throws IOException{
		
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass, false);
		
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
		
	}
	
	@Test
	public void shouldPopulateObjectWithNameAnnotation(){
		Class<?> baseClass = configModel.getClass();
		assertNull(configModel.getConfigString());
		configLoader.load(configModel, baseClass, false);
		
		assertNotNull(configModel.getConfigStringWithName());
		assertEquals(UtilTest.CONFIG_STRING_NAME_ANNOTATION_VALUE, configModel.getConfigStringWithName());
	}
	
	@Test(expected = ConfigurationException.class)
	public void objectWithNoMatchExtractorShouldThrowException(){
		
		ConfigWithoutExtractorModel model = new ConfigWithoutExtractorModel();
		
		Class<?> baseClass = model.getClass();
		
		configLoader.load(model, baseClass, false);
	}

}
