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
package configuration.field.primitiveorwrapper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;
import static org.junit.Assert.assertNull;

import javax.inject.Inject;

import org.apache.commons.configuration.ConversionException;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;

@RunWith(Arquillian.class)
public class ConfigurationPrimitiveOrWrapperFieldTest {

	private static final String PATH = "src/test/resources/configuration/field/primitiveorwrapper";

	@Inject
	private PropertiesPrimitiveOrWrapperFieldConfig propertiesConfig;

	@Inject
	private XMLPrimitiveOrWrapperFieldConfig xmlConfig;

	@Inject
	private SystemPrimitiveOrWrapperFieldConfig systemConfig;

	@Inject
	private PropertiesPrimitiveOrWrapperErrorFieldConfig propertiesErrorConfig;

	@Inject
	private XMLPrimitiveOrWrapperErrorFieldConfig xmlErrorConfig;

	@Inject
	private SystemPrimitiveOrWrapperErrorFieldConfig systemErrorConfig;

	@Inject
	private PropertiesNullWrappedField nullWrappedField;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(ConfigurationPrimitiveOrWrapperFieldTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.xml"), "demoiselle.xml");
		return deployment;
	}

	@BeforeClass
	public static void afterClass() {
		System.setProperty("primitiveField", String.valueOf(1));
		System.setProperty("emptyPrimitiveField", String.valueOf(""));
		System.setProperty("errorPrimitiveField", String.valueOf("a"));
		System.setProperty("wrappedField", String.valueOf(2));
		System.setProperty("emptyWrappedField", String.valueOf(""));
		System.setProperty("errorWrappedField", String.valueOf("a"));
	}

	@Test
	public void loadPrimitiveField() {
		int expected = 1;

		assertEquals(expected, systemConfig.getPrimitiveField());
		assertEquals(expected, propertiesConfig.getPrimitiveField());
		assertEquals(expected, xmlConfig.getPrimitiveField());
	}

	@Test
	public void loadWrappedField() {
		Integer expected = 2;

		assertEquals(expected, systemConfig.getWrappedField());
		assertEquals(expected, propertiesConfig.getWrappedField());
		assertEquals(expected, xmlConfig.getWrappedField());
	}

	@Test
	public void loadEmptyPrimitiveField() {
		try {
			systemErrorConfig.getEmptyPrimitiveField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			propertiesErrorConfig.getEmptyPrimitiveField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			propertiesErrorConfig.getEmptyPrimitiveField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadEmptyWrappedField() {
		try {
			systemErrorConfig.getEmptyWrappedField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			propertiesErrorConfig.getEmptyWrappedField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			propertiesErrorConfig.getEmptyWrappedField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadErrorPrimitiveField() {
		try {
			propertiesErrorConfig.getConversionErrorPrimitiveField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			propertiesErrorConfig.getConversionErrorPrimitiveField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			xmlErrorConfig.getConversionErrorPrimitiveField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadErrorWrappedField() {
		try {
			propertiesErrorConfig.getConversionErrorWrappedField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			propertiesErrorConfig.getConversionErrorWrappedField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}

		try {
			xmlErrorConfig.getConversionErrorWrappedField();
			fail();
		} catch (ConfigurationException cause) {
			assertEquals(ConversionException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadNullWrappedField() {
		assertNull(nullWrappedField.getNullWrappedField());
	}
}
