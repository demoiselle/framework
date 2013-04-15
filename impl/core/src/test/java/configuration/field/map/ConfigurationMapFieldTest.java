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
package configuration.field.map;

import static junit.framework.Assert.assertEquals;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.asset.FileAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import configuration.ConfigurationTests;

@RunWith(Arquillian.class)
public class ConfigurationMapFieldTest {

	@Inject
	private PropertiesMapFieldConfig propertiesConfig;

	@Inject
	private XMLMapFieldConfig xmlConfig;

	@Deployment
	public static JavaArchive createDeployment() {
		JavaArchive deployment = ConfigurationTests.createDeployment(ConfigurationMapFieldTest.class);
		deployment.addAsResource(
				new FileAsset(new File("src/test/resources/configuration/field/map/demoiselle.properties")),
				"demoiselle.properties").addAsResource(
				new FileAsset(new File("src/test/resources/configuration/field/map/demoiselle.xml")), "demoiselle.xml");

		return deployment;
	}

	@Test
	public void loadStringWithDefinedKeyMap() {
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("item1", "demoiselle");
		expected.put("item2", "framework");

		assertEquals(expected, propertiesConfig.getStringWithDefinedKeyMap());
		assertEquals(expected, xmlConfig.getStringWithDefinedKeyMap());
	}

	@Test
	public void loadStringWithUndefinedKeyMap() {
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("default", "undefined");

		assertEquals(expected, propertiesConfig.getStringWithUndefinedKeyMap());
		assertEquals(expected, xmlConfig.getStringWithUndefinedKeyMap());
	}

	@Test
	public void loadEmptyKeyMapString() {
		Map<String, String> expected = new HashMap<String, String>();
		expected.put("item1", "");
		expected.put("item2", "");

		assertEquals(expected, propertiesConfig.getEmptyValueMap());
		assertEquals(expected, xmlConfig.getEmptyValueMap());
	}
}
