package org.demoiselle.jee.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
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


public class ArrayExtractorTest {
	
	private final UtilTest utilTest = new UtilTest();
	
	private String FILE_PATH_PROPERTIES = "";
	private String FILE_PATH_XML = "";
	
	private final String FILE_PREFIX = "app";
	private final String PREFIX = "";
	
	private ConfigurationArrayValueExtractor conf = new ConfigurationArrayValueExtractor();
	private ConfigModel configModel = new ConfigModel();
	
	
	public ArrayExtractorTest() throws FileNotFoundException, IOException {
		FILE_PATH_PROPERTIES = utilTest.createPropertiesFile(FILE_PREFIX);
		FILE_PATH_XML = utilTest.createXMLFile(FILE_PREFIX);
		utilTest.createSystemVariables();
	}
	
	@Test
	public void extractMapFromProperties() throws Exception{
		Configuration configuration = utilTest.buildConfiguration(PropertiesConfiguration.class, FILE_PATH_PROPERTIES);
		
		Field field = configModel.getClass().getDeclaredField(UtilTest.CONFIG_ARRAY_FIELD);
		
		String[] result = (String[]) conf.getValue(PREFIX, UtilTest.CONFIG_ARRAY_FIELD, field, configuration);
		
		List<String> list = Arrays.asList(result);
		
		assertEquals(UtilTest.CONFIG_ARRAY_VALUE.getClass(), result.getClass());
		assertEquals(3, result.length);
		assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_1));
		assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_2));
		assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_3));
	}
	
	@Test
	public void extractMapFromXML() throws Exception{
		Configuration configuration = utilTest.buildConfiguration(XMLConfiguration.class, FILE_PATH_XML);
		
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
	System roperties doesn't accept duplicate key, so I need test Array Extractor. 
	@Test	
	public void extractMapFromSystem() throws Exception{
		BasicConfigurationBuilder<? extends Configuration> builder = new BasicConfigurationBuilder<>(SystemConfiguration.class);
		
		Field field = configModel.getClass().getDeclaredField(UtilTest.CONFIG_ARRAY_FIELD);
		
		String[] result = (String[]) conf.getValue(PREFIX, UtilTest.CONFIG_ARRAY_FIELD, field, builder.getConfiguration());
		
		List<String> list = Arrays.asList(result);
		
		assertEquals(UtilTest.CONFIG_ARRAY_VALUE.getClass(), result.getClass());
		assertEquals(3, result.length);
		assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_1));
		assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_2));
		assertTrue(list.contains(UtilTest.CONFIG_ARRAY_VALUE_3));
	}*/
	
	@Test
	public void extractorShouldBeSupportArray() throws NoSuchFieldException, SecurityException{
		assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_ARRAY_FIELD)));
	}

}
