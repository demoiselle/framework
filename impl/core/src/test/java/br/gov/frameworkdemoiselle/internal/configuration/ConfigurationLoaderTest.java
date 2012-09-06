/*
 * Demoiselle Framework
 * Copyright (C) 2010 SERPRO
 * ----------------------------------------------------------------------------
 * This file is part of Demoiselle Framework.
 * 
 * Demoiselle Framework is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License version 3
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License version 3
 * along with this program; if not,  see <http://www.gnu.org/licenses/>
 * or write to the Free Software Foundation, Inc., 51 Franklin Street,
 * Fifth Floor, Boston, MA  02110-1301, USA.
 * ----------------------------------------------------------------------------
 * Este arquivo é parte do Framework Demoiselle.
 * 
 * O Framework Demoiselle é um software livre; você pode redistribuí-lo e/ou
 * modificá-lo dentro dos termos da GNU LGPL versão 3 como publicada pela Fundação
 * do Software Livre (FSF).
 * 
 * Este programa é distribuído na esperança que possa ser útil, mas SEM NENHUMA
 * GARANTIA; sem uma garantia implícita de ADEQUAÇÃO a qualquer MERCADO ou
 * APLICAÇÃO EM PARTICULAR. Veja a Licença Pública Geral GNU/LGPL em português
 * para maiores detalhes.
 * 
 * Você deve ter recebido uma cópia da GNU LGPL versão 3, sob o título
 * "LICENCA.txt", junto com esse programa. Se não, acesse <http://www.gnu.org/licenses/>
 * ou escreva para a Fundação do Software Livre (FSF) Inc.,
 * 51 Franklin St, Fifth Floor, Boston, MA 02111-1301, USA.
 */
package br.gov.frameworkdemoiselle.internal.configuration;

import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.powermock.api.easymock.PowerMock.mockStatic;

import java.util.Locale;

import javax.validation.constraints.NotNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import br.gov.frameworkdemoiselle.annotation.Ignore;
import br.gov.frameworkdemoiselle.annotation.Name;
import br.gov.frameworkdemoiselle.configuration.ConfigType;
import br.gov.frameworkdemoiselle.configuration.Configuration;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;
import br.gov.frameworkdemoiselle.internal.bootstrap.CoreBootstrap;
import br.gov.frameworkdemoiselle.util.Beans;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Beans.class)
public class ConfigurationLoaderTest {

	private ConfigurationLoader configurationLoader;

	private CoreBootstrap coreBootstrap;
	
	@Configuration
	public class ConfigurationSuccessfulProperties {

		@Name("frameworkdemoiselle.configurationtest.nameConfiguration")
		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationSuccessfulProperties2 {

		@Name("frameworkdemoiselle.configurationtest.name")
		protected String name;

	}

	@Configuration(resource = "absentFile")
	public class ConfigurationPropertiesWithAbsentFile {

		@Name("frameworkdemoiselle.configurationtest.nameConfiguration")
		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationWithEmptyName {

		@Name("")
		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationWithoutNameAnnotation {

		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationWithIgnoreAnnotation {

		@Ignore
		protected String nameConfiguration;

	}

	@Configuration(prefix = "frameworkdemoiselle.configurationtest")
	public class ConfigurationWithPrefix {

		@Name("nameConfiguration")
		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationWithKeyNotFoundInProperties {

		protected Integer notExistKey;
	}

	@Configuration
	public class ConfigurationWithNotNullFieldButValueIsNull {

		@Name("notexistKey")
		@NotNull
		protected int nameConfiguration;

	}

	@Configuration
	public class ConfigurationWithNotNullFieldAndValueIsNotNull {

		@Name("nameConfiguration")
		@NotNull
		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationWithNonPrimitiveFieldValueNull {

		@Name("notexistKey")
		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationWithPrimitiveFieldValueNull {

		@Name("notexistKey")
		protected int nameConfiguration = 1;

	}

	@Configuration(type = ConfigType.SYSTEM)
	public class ConfigurationWithKeyFromSystem {

		@Name("os.name")
		protected String nameConfiguration;

	}

	@Configuration(type = ConfigType.XML)
	public class ConfigurationWithKeyFromXML {

		@Name("nameConfiguration")
		protected String nameConfiguration;

	}

	@Configuration(type = ConfigType.XML, prefix = "br.gov.frameworkdemoiselle")
	public class ConfigurationFromXMLWithPrefix {

		@Name("nameConfiguration")
		protected String nameConfiguration;

	}

	@Configuration
	public class ConfigurationPropertiesWithTwoAmbiguousKey {

		protected String twoConfiguration;

	}

	@Configuration
	public class ConfigurationPropertiesWithThreeAmbiguousKey {

		protected String threeConfiguration;

	}

	@Configuration
	public class ConfigurationPropertiesWithFourAmbiguousKey {

		protected String fourConfiguration;

	}

	@Configuration
	public class ConfigurationWithConventionUnderline {

		protected String conventionUnderline;

	}

	@Configuration(type = ConfigType.XML)
	public class ConfigurationXMLWithConventionUnderline {

		protected String conventionUnderline;

	}

	@Configuration
	public class ConfigurationWithConventionDot {

		protected String conventionDot;

	}

	@Configuration(type = ConfigType.XML)
	public class ConfigurationXMLWithConventionDot {

		protected String conventionDot;

	}

	@Configuration
	public class ConfigurationWithConventionAllUpperCase {

		protected String conventionAllUpperCase;

	}

	@Configuration(type = ConfigType.XML)
	public class ConfigurationXMLWithConventionAllUpperCase {

		protected String conventionAllUpperCase;

	}

	@Configuration
	public class ConfigurationWithConventionAllLowerCase {

		protected String conventionAllLowerCase;

	}

	@Configuration(type = ConfigType.XML)
	public class ConfigurationXMLWithConventionAllLowerCase {

		protected String conventionAllLowerCase;

	}

	@Configuration(prefix = "br.gov.frameworkdemoiselle")
	public class ConfigurationPropertiesSuccessWithPrefixNonAmbiguous {

		protected String success;

	}

	@Configuration
	public class ConfigurationPropertiesErrorWithComplexObject {

		protected ConfigurationWithConventionAllLowerCase complexObject;
	}

	@Before
	public void setUp() throws Exception {
		this.configurationLoader = new ConfigurationLoader();
		mockStatic(Beans.class);
		this.coreBootstrap = PowerMock.createMock(CoreBootstrap.class);
		
		expect(Beans.getReference(CoreBootstrap.class)).andReturn(coreBootstrap);
		expect(Beans.getReference(Locale.class)).andReturn(Locale.getDefault());
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testConfigurationSuccessfulPropertiesPossibleConventions() {
		ConfigurationSuccessfulProperties config = new ConfigurationSuccessfulProperties();
		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		configurationLoader.load(config);
		assertEquals("ConfigurationTest", config.nameConfiguration);

	}

	@Test
	public void testConfigurationSuccessfulPropertiesNoConventions() {
		ConfigurationSuccessfulProperties2 config = new ConfigurationSuccessfulProperties2();
		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		configurationLoader.load(config);
		assertEquals("ConfigurationTest2", config.name);
	}

	@Test
	public void ConfigurationPropertiesWithAbsentFile() {
		ConfigurationPropertiesWithAbsentFile config = new ConfigurationPropertiesWithAbsentFile();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		try {
			configurationLoader.load(config);
			fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void testConfigurationProcessorWithNameEmpty() {
		ConfigurationWithEmptyName config = new ConfigurationWithEmptyName();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		try {
			configurationLoader.load(config);
			fail();
		} catch (Exception e) {
		}
	}

	@Test
	public void testConfigurationWithoutNameAnnotation() {
		ConfigurationWithoutNameAnnotation config = new ConfigurationWithoutNameAnnotation();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("ConfigurationTest", config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithIgnoreAnnotation() {
		ConfigurationWithIgnoreAnnotation config = new ConfigurationWithIgnoreAnnotation();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertNull(config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithPrefix() {
		ConfigurationWithPrefix config = new ConfigurationWithPrefix();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("ConfigurationTest", config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithKeyNotFoundInProperties() {
		ConfigurationWithKeyNotFoundInProperties config = new ConfigurationWithKeyNotFoundInProperties();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertNull(config.notExistKey);
	}

	@Test
	public void testConfigurationWithNotNullFieldButValueIsNull() {
		ConfigurationWithNotNullFieldButValueIsNull config = new ConfigurationWithNotNullFieldButValueIsNull();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		try {
			configurationLoader.load(config);
			fail();
		} catch (Exception e) {
			assertTrue(true);
		}
	}

	@Test
	public void testConfigurationWithNotNullFieldAndValueIsNotNull() {
		ConfigurationWithNotNullFieldAndValueIsNotNull config = new ConfigurationWithNotNullFieldAndValueIsNotNull();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("ConfigurationTest", config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithNonPrimitiveFieldValueNull() {
		ConfigurationWithNonPrimitiveFieldValueNull config = new ConfigurationWithNonPrimitiveFieldValueNull();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertNull(config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithPrimitiveFieldValueNull() {
		ConfigurationWithPrimitiveFieldValueNull config = new ConfigurationWithPrimitiveFieldValueNull();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertEquals(1, config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithKeyFromSystem() {
		ConfigurationWithKeyFromSystem config = new ConfigurationWithKeyFromSystem();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals(System.getProperty("os.name"), config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithKeyFromXML() {
		ConfigurationWithKeyFromXML config = new ConfigurationWithKeyFromXML();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("ConfigurationTest", config.nameConfiguration);
	}

	@Test
	public void testConfigurationWithTwoAmbiguousKey() {
		ConfigurationPropertiesWithTwoAmbiguousKey config = new ConfigurationPropertiesWithTwoAmbiguousKey();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		try {
			configurationLoader.load(config);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ConfigurationException);
		}

	}

	@Test
	public void testConfigurationWithThreeAmbiguousKey() {
		ConfigurationPropertiesWithThreeAmbiguousKey config = new ConfigurationPropertiesWithThreeAmbiguousKey();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		try {
			configurationLoader.load(config);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ConfigurationException);
		}

	}

	@Test
	public void testConfigurationWithFourAmbiguousKey() {
		ConfigurationPropertiesWithFourAmbiguousKey config = new ConfigurationPropertiesWithFourAmbiguousKey();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		try {
			configurationLoader.load(config);
			fail();
		} catch (Exception e) {
			assertTrue(e instanceof ConfigurationException);
		}

	}

	@Test
	public void testConfigurationWithPrefixNotAmbiguous() {
		ConfigurationPropertiesSuccessWithPrefixNonAmbiguous config = new ConfigurationPropertiesSuccessWithPrefixNonAmbiguous();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("Success", config.success);
	}

	@Test
	public void testConfigurationWithConventionUnderline() {
		ConfigurationWithConventionUnderline config = new ConfigurationWithConventionUnderline();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("Convention Underline", config.conventionUnderline);
	}

	@Test
	public void testConfigurationWithConventionDot() {
		ConfigurationWithConventionDot config = new ConfigurationWithConventionDot();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("Convention Dot", config.conventionDot);
	}

	@Test
	public void testConfigurationWithConventionAllLowerCase() {
		ConfigurationWithConventionAllLowerCase config = new ConfigurationWithConventionAllLowerCase();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("All LowerCase", config.conventionAllLowerCase);
	}

	@Test
	public void testConfigurationWithConventionAllUpperCase() {
		ConfigurationWithConventionAllUpperCase config = new ConfigurationWithConventionAllUpperCase();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);

		configurationLoader.load(config);
		assertEquals("ALL UPPERCASE", config.conventionAllUpperCase);
	}

	@Test
	public void testConfigurationPropertiesErrorWithComplexObject() {
		ConfigurationPropertiesErrorWithComplexObject config = new ConfigurationPropertiesErrorWithComplexObject();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		try {
			configurationLoader.load(config);
			fail();
		} catch (Throwable throwable) {
		}
	}

	@Test
	public void testConfigurationFromXMLWithPrefix() {
		ConfigurationFromXMLWithPrefix config = new ConfigurationFromXMLWithPrefix();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertEquals("ConfigurationTest", config.nameConfiguration);
	}

	@Test
	public void testConfigurationXMLWithConventionDot() {
		ConfigurationXMLWithConventionDot config = new ConfigurationXMLWithConventionDot();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertEquals("convention.dot", config.conventionDot);
	}

	@Test
	public void testConfigurationXMLWithConventionUnderline() {
		ConfigurationXMLWithConventionUnderline config = new ConfigurationXMLWithConventionUnderline();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertEquals("Convention_Underline", config.conventionUnderline);
	}

	@Test
	public void testConfigurationXMLWithConventionAllUpperCase() {
		ConfigurationXMLWithConventionAllUpperCase config = new ConfigurationXMLWithConventionAllUpperCase();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertEquals("ALL UPPERCASE", config.conventionAllUpperCase);
	}

	@Test
	public void testConfigurationXMLWithConventionAllLowerCase() {
		ConfigurationXMLWithConventionAllLowerCase config = new ConfigurationXMLWithConventionAllLowerCase();

		expect(coreBootstrap.isAnnotatedType(config.getClass())).andReturn(true);
		PowerMock.replayAll(CoreBootstrap.class,Beans.class);
		
		configurationLoader.load(config);
		assertEquals("All LowerCase", config.conventionAllLowerCase);
	}

}