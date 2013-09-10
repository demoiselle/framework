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
package configuration.field.beanvalidation;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.fail;

import javax.inject.Inject;
import javax.validation.ConstraintViolationException;

import junit.framework.Assert;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import test.Tests;
import br.gov.frameworkdemoiselle.configuration.ConfigurationException;

@RunWith(Arquillian.class)
public class ConfigurationBeanValidationFieldTest {

	private static final String PATH = "src/test/resources/configuration/field/beanvalidation";

	@Inject
	private PropertyBeanValidationWithFIlledNotNullFieldConfig propertyBeanValidationWithFIlledNotNullFieldConfig;

	@Inject
	private XMLBeanValidationWithFilledNotNullFieldConfig xmlBeanValidationWithFilledNotNullFieldConfig;

	@Inject
	private PropertyBeanValidationWithEmptyNotNullFieldConfig propertyBeanValidationWithEmptyNotNullFieldConfig;

	@Inject
	private XMLBeanValidationWithEmptyNotNullFieldConfig xmlBeanValidationWithEmptyNotNullFieldConfig;

	@Inject
	private PropertyWithTwoConstrainViolations propertyWithTwoConstrainViolations;

	@Deployment
	public static JavaArchive createDeployment() {

		JavaArchive deployment = Tests.createDeployment(ConfigurationBeanValidationFieldTest.class);
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.properties"), "demoiselle.properties");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/demoiselle.xml"), "demoiselle.xml");
		deployment.addAsResource(Tests.createFileAsset(PATH + "/two-constrain-validation.properties"),
				"two-constrain-validation.properties");

		return deployment;
	}

	@Test
	public void loadIntegerNotNullFieldFromFilledFile() {
		Integer expectedInt = 1;

		assertEquals(expectedInt, propertyBeanValidationWithFIlledNotNullFieldConfig.getIntAttibuteNotNull());
		assertEquals(expectedInt, xmlBeanValidationWithFilledNotNullFieldConfig.getIntAttibuteNotNull());
	}

	@Test
	public void loadStringNotNullFieldFromFilledFile() {
		String expectedString = "Not null!";

		assertEquals(expectedString, propertyBeanValidationWithFIlledNotNullFieldConfig.getStringAttributeNotNull());
		assertEquals(expectedString, xmlBeanValidationWithFilledNotNullFieldConfig.getStringAttributeNotNull());
	}

	@Test
	public void loadIntNotNullFieldFromNotFilledFile() {
		try {
			propertyBeanValidationWithEmptyNotNullFieldConfig.getIntAttributeNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(ConstraintViolationException.class, cause.getCause().getClass());
		}

		try {
			xmlBeanValidationWithEmptyNotNullFieldConfig.getIntAttributeNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(ConstraintViolationException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadStringNotNullFieldFromNotFilledFile() {
		try {
			propertyBeanValidationWithEmptyNotNullFieldConfig.getStringAttributeNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(ConstraintViolationException.class, cause.getCause().getClass());
		}

		try {
			xmlBeanValidationWithEmptyNotNullFieldConfig.getStringAttributeNull();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(ConstraintViolationException.class, cause.getCause().getClass());
		}
	}

	@Test
	public void loadPropertiesWithTwoConstraintViolations() {
		try {
			propertyWithTwoConstrainViolations.getAttributeWithTwoConstrainValidations();
			fail();
		} catch (ConfigurationException cause) {
			Assert.assertEquals(ConstraintViolationException.class, cause.getCause().getClass());
		}
	}
}
