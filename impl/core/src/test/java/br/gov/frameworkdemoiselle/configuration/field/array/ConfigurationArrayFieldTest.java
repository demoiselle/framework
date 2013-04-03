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
package br.gov.frameworkdemoiselle.configuration.field.array;

import static org.junit.Assert.assertArrayEquals;

import java.io.File;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import br.gov.frameworkdemoiselle.configuration.AbstractConfigurationTest;

@RunWith(Arquillian.class)
public class ConfigurationArrayFieldTest extends AbstractConfigurationTest {

	@Inject
	private PropertiesArrayFieldConfig propertiesConfig;
	
	@Inject
	private XMLArrayFieldConfig xmlConfig;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = createConfigurationDeployment();

		deployment.addPackages(true, ConfigurationArrayFieldTest.class.getPackage());
		deployment.addAsResource(new FileAsset(new File(
				"src/test/resources/configuration/field/array/demoiselle.properties")), "demoiselle.properties")
				.addAsResource(new FileAsset(new File("src/test/resources/configuration/field/array/demoiselle.xml")),
						"demoiselle.xml");

		return deployment;
	}

	@Test
	public void loadPrimitiveInteger() {
		int[] expected = { 1, 20, 0 };

		assertArrayEquals(expected, propertiesConfig.getPrimitiveIntegers());
		assertArrayEquals(expected, xmlConfig.getPrimitiveIntegers());
	}

	@Test
	public void loadWrappedIntegers() {
		Integer[] expected = { -99, 0, 1 };

		assertArrayEquals(expected, propertiesConfig.getWrappedIntegers());
		assertArrayEquals(expected, xmlConfig.getWrappedIntegers());
	}

	@Test
	public void loadString() {
		String[] expected = { "demoisele", "demoiselle framework", "demoiselle,framework", "demoiselle, framework" };
		
		assertArrayEquals(expected, propertiesConfig.getStrings());
		assertArrayEquals(expected, xmlConfig.getStrings());
	}
	
	@Test
	public void loadPrimitiveDoubles(){
		double[] expected = { -10, 200000.99999, 0 };

		assertArrayEquals(expected, propertiesConfig.getPrimitiveDoubles(), 0);
		assertArrayEquals(expected, xmlConfig.getPrimitiveDoubles(), 0);
	}
	
	@Test
	public void loadWrappedDoubles() {
		Double[] expected = { -456.123456789, 0.0, 52.2 };

		assertArrayEquals(expected, propertiesConfig.getWrappedDoubles());
		assertArrayEquals(expected, xmlConfig.getWrappedDoubles());
	}
}
