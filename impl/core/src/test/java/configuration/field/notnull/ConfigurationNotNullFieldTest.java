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
package configuration.field.notnull;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import javax.inject.Inject;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import util.Tests;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;

@RunWith(Arquillian.class)
public class ConfigurationNotNullFieldTest {

	private static final String PATH = "src/test/resources/configuration/field/notnull";

	@Inject
	private PropertyWithFilledFieldConfig propertyFilledFieldConfig;

	@Inject
	private PropertyWithEmptyFieldConfig propertyEmptyFieldsConfig;

	@Inject
	private PropertyWithoutNotNullField propertyWithoutNotNullField;

	@Inject
	private PropertyWithoutFileConfig propertyNoFileConfig;

	@Inject
	private XMLWithFilledFieldConfig xmlFilledFieldConfig;

	@Inject
	private XMLWithEmptyFieldConfig xmlEmptyFieldsConfig;

	@Inject
	private XMLWithoutNotNullField xmlWithoutNotNullField;

	@Inject
	private XMLWithoutFileConfig xmlNoFileConfig;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = Tests.createDeployment(ConfigurationNotNullFieldTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/empty-field.properties"), "empty-field.properties");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/without-field.properties"), "without-field.properties");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.xml"), "demoiselle.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/empty-field.xml"), "empty-field.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/without-field.xml"), "without-field.xml");
		return deployment;
	}

	@Test
	public void loadFieldNotNullFromFilledFile() {
		Integer expected = 1;

		assertEquals(expected, propertyFilledFieldConfig.getAttributeNotNull());
		assertEquals(expected, xmlFilledFieldConfig.getAttributeNotNull());
	}

	@Test
	public void loadFieldNotNullFromEmptyProperty() {
		try {
			propertyEmptyFieldsConfig.getAttributeNotNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(NullPointerException.class, cause.getCause().getClass());
		}

		try {
			xmlEmptyFieldsConfig.getAttributeNotNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(NullPointerException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadFieldFromPropertyFileWithoutNotNullField() {
		try {
			propertyWithoutNotNullField.getAttributeNotNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(NullPointerException.class, cause.getCause().getClass());
		}

		try {
			xmlWithoutNotNullField.getAttributeNotNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(NullPointerException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadFieldNotNullFromInexistentPropertyFile() {
		try {
			propertyNoFileConfig.getAttributeNotNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(NullPointerException.class, cause.getCause().getClass());
		}

		try {
			xmlNoFileConfig.getAttributeNotNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(NullPointerException.class, cause.getCause().getClass());
		}
	}
}
