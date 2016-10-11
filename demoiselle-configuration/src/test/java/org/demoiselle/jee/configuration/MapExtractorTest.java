package org.demoiselle.jee.configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.Test;


public class MapExtractorTest {
	
	private final UtilTest utilTest = new UtilTest();
	
	private String FILE_PATH_PROPERTIES = "";
	private String FILE_PATH_XML = "";
	
	private final String FILE_PREFIX = "app";
	private final String PREFIX = "";
	
	private ConfigurationMapValueExtractor conf = new ConfigurationMapValueExtractor();
	private ConfigModel configModel = new ConfigModel();
	
	private String keyIp = "";
	private String keyPort = "";
	private String keyProtocol = "";
	
	public MapExtractorTest() throws FileNotFoundException, IOException {
		FILE_PATH_PROPERTIES = utilTest.createPropertiesFile(FILE_PREFIX);
		FILE_PATH_XML = utilTest.createXMLFile(FILE_PREFIX);
		utilTest.createSystemVariables();
		
		keyIp = UtilTest.CONFIG_MAP_FIELD_IP.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		keyPort = UtilTest.CONFIG_MAP_FIELD_PORT.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		keyProtocol = UtilTest.CONFIG_MAP_FIELD_PROTOCOL.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
	}
	
	@Test
	public void extractMapFromProperties() throws Exception{
		Configuration configuration = utilTest.buildConfiguration(PropertiesConfiguration.class, FILE_PATH_PROPERTIES);
		
		Field field = configModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD);
		
		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>) conf.getValue(PREFIX, UtilTest.CONFIG_MAP_FIELD, field, configuration);
		
		assertEquals(UtilTest.CONFIG_MAP_VALUE.getClass(), result.getClass());
		assertEquals(UtilTest.CONFIG_MAP_VALUE_IP, result.get(keyIp));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PORT, result.get(keyPort));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PROTOCOL, result.get(keyProtocol));
	}
	
	@Test
	public void extractMapFromXML() throws Exception{
		Configuration configuration = utilTest.buildConfiguration(XMLConfiguration.class, FILE_PATH_XML);
		
		Field field = configModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD);
		
		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>) conf.getValue(PREFIX, UtilTest.CONFIG_MAP_FIELD, field, configuration);
		
		assertEquals(UtilTest.CONFIG_MAP_VALUE.getClass(), result.getClass());
		assertEquals(UtilTest.CONFIG_MAP_VALUE_IP, result.get(keyIp));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PORT, result.get(keyPort));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PROTOCOL, result.get(keyProtocol));
	}
	
	@Test
	public void extractMapFromSystem() throws Exception{
		BasicConfigurationBuilder<? extends Configuration> builder = new BasicConfigurationBuilder<>(SystemConfiguration.class);
		
		Field field = configModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD);
		
		@SuppressWarnings("unchecked")
		Map<String, String> result = (Map<String, String>) conf.getValue(PREFIX, UtilTest.CONFIG_MAP_FIELD, field, builder.getConfiguration());
		
		assertEquals(UtilTest.CONFIG_MAP_VALUE.getClass(), result.getClass());
		assertEquals(UtilTest.CONFIG_MAP_VALUE_IP, result.get(keyIp));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PORT, result.get(keyPort));
		assertEquals(UtilTest.CONFIG_MAP_VALUE_PROTOCOL, result.get(keyProtocol));
	}
	
	@Test
	public void extractorShouldBeSupportMap() throws NoSuchFieldException, SecurityException{
		assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD)));
	}

}
