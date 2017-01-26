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
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.PropertiesConfiguration;
import org.apache.commons.configuration2.SystemConfiguration;
import org.apache.commons.configuration2.XMLConfiguration;
import org.apache.commons.configuration2.builder.BasicConfigurationBuilder;
import org.demoiselle.jee.configuration.extractor.impl.ConfigurationMapValueExtractor;
import org.demoiselle.jee.configuration.model.ConfigMapModel;
import org.demoiselle.jee.configuration.model.ConfigModel;
import org.demoiselle.jee.configuration.util.UtilTest;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author SERPRO
 *
 */
public class MapExtractorTest extends AbstractConfigurationTest{
	
	private ConfigurationMapValueExtractor conf = new ConfigurationMapValueExtractor();
	private ConfigModel configModel = new ConfigModel();
	
	private String keyIp = "";
	private String keyPort = "";
	private String keyProtocol = "";
	private String keyWithHifen = "";
	
	@Before
	public void setUp(){
		keyIp = UtilTest.CONFIG_MAP_FIELD_IP.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		keyPort = UtilTest.CONFIG_MAP_FIELD_PORT.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		keyProtocol = UtilTest.CONFIG_MAP_FIELD_PROTOCOL.replaceAll(UtilTest.CONFIG_MAP_FIELD + ".", "");
		keyWithHifen = UtilTest.CONFIG_MAP_FIELD_WITH_HIFEN.substring(UtilTest.CONFIG_MAP_FIELD_WITH_HIFEN.lastIndexOf(".") + 1, UtilTest.CONFIG_MAP_FIELD_WITH_HIFEN.length());
	}
	
	@Test
	public void extractMapFromProperties() throws Exception{
		Configuration configuration = utilTest.buildConfiguration(PropertiesConfiguration.class, utilTest.createPropertiesFile(FILE_PREFIX));
		
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
		Configuration configuration = utilTest.buildConfiguration(XMLConfiguration.class, utilTest.createXMLFile(FILE_PREFIX));
		
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
		utilTest.createSystemVariables();
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
	public void extractMapFromPropertiesWithPrefixAndSuffixAndHifen() throws Exception{
	    Configuration configuration = utilTest.buildConfiguration(PropertiesConfiguration.class, utilTest.createPropertiesFile(FILE_PREFIX));
	    
	    ConfigMapModel configMapModel = new ConfigMapModel();
	    Field field = configMapModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD);
        
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) conf.getValue("demoiselle.configuration.", UtilTest.CONFIG_MAP_FIELD, field, configuration);
        
        assertEquals(UtilTest.CONFIG_MAP_VALUE.getClass(), result.getClass());
        assertEquals(UtilTest.CONFIG_MAP_VALUE_WITH_HIFEN, result.get(keyWithHifen));
	}
	
	@Test
    public void extractMapFromXMLWithPrefixAndSuffixAndHifen() throws Exception{
        Configuration configuration = utilTest.buildConfiguration(XMLConfiguration.class, utilTest.createXMLFile(FILE_PREFIX));
        
        ConfigMapModel configMapModel = new ConfigMapModel();
        Field field = configMapModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD);
        
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) conf.getValue("demoiselle.configuration.", UtilTest.CONFIG_MAP_FIELD, field, configuration);
        
        assertEquals(UtilTest.CONFIG_MAP_VALUE.getClass(), result.getClass());
        assertEquals(UtilTest.CONFIG_MAP_VALUE_WITH_HIFEN, result.get(keyWithHifen));
    }
	
	@Test
    public void extractMapFromSystemWithPrefixAndSuffixAndHifen() throws Exception{
	    utilTest.createSystemVariables();
	    BasicConfigurationBuilder<? extends Configuration> builder = new BasicConfigurationBuilder<>(SystemConfiguration.class);
	    
        ConfigMapModel configMapModel = new ConfigMapModel();
        Field field = configMapModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD);
        
        @SuppressWarnings("unchecked")
        Map<String, String> result = (Map<String, String>) conf.getValue("demoiselle.configuration.", UtilTest.CONFIG_MAP_FIELD, field, builder.getConfiguration());
        
        assertEquals(UtilTest.CONFIG_MAP_VALUE.getClass(), result.getClass());
        assertEquals(UtilTest.CONFIG_MAP_VALUE_WITH_HIFEN, result.get(keyWithHifen));
    }
	
	@Test
	public void extractorShouldBeSupportMap() throws NoSuchFieldException, SecurityException{
		assertTrue(conf.isSupported(configModel.getClass().getDeclaredField(UtilTest.CONFIG_MAP_FIELD)));
	}

}
